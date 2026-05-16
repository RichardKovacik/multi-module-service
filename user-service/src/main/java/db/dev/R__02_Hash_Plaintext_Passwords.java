package db.dev;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class R__02_Hash_Plaintext_Passwords extends BaseJavaMigration {

    @Override
    public void migrate(org.flywaydb.core.api.migration.Context context) throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        var select = context.getConnection()
                .prepareStatement("SELECT id, password FROM users WHERE password NOT LIKE '$2%'");

        ResultSet rs = select.executeQuery();

        while (rs.next()) {
            long id = rs.getLong("id");
            String plain = rs.getString("password");
            String hashed = encoder.encode(plain);

            PreparedStatement update = context.getConnection()
                    .prepareStatement("UPDATE users SET password = ? WHERE id = ?");
            update.setString(1, hashed);
            update.setLong(2, id);
            update.executeUpdate();
        }
    }
}

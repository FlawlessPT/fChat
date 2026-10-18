package pt.flawless.fchat.prefs;

import pt.flawless.fapi.chat.Channel;
import pt.flawless.fapi.database.DatabaseService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** {@link ChatPrefsRepository} sobre a tabela {@code fchat_prefs}. */
public class SqlChatPrefsRepository implements ChatPrefsRepository {

    private static final String SELECT = "SELECT default_channel FROM fchat_prefs WHERE uuid = ?";
    private static final String UPSERT =
            "INSERT INTO fchat_prefs (uuid, default_channel) VALUES (?, ?) AS new "
                    + "ON DUPLICATE KEY UPDATE default_channel = new.default_channel";

    private final DatabaseService database;

    public SqlChatPrefsRepository(DatabaseService database) {
        this.database = database;
    }

    @Override
    public CompletableFuture<Optional<Channel>> find(UUID uuid) {
        return database.supplyAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(SELECT)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(Channel.valueOf(rs.getString(1))) : Optional.<Channel>empty();
                }
            }
        });
    }

    @Override
    public CompletableFuture<Void> save(UUID uuid, Channel channel) {
        return database.runAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPSERT)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, channel.name());
                ps.executeUpdate();
            }
        });
    }
}

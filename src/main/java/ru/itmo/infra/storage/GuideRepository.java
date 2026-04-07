package ru.itmo.infra.storage;

import lombok.extern.java.Log;
import ru.itmo.domain.model.GuideSection;
import ru.itmo.domain.model.GuideSubsection;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
public class GuideRepository {

    public static List<GuideSection> findAllActiveSectionsOrdered() throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                SELECT id, slug, title, menu_order, command, is_active, is_hidden
                FROM guide_section
                WHERE is_active = TRUE
                ORDER BY menu_order;
                """)) {
            var rs = statement.executeQuery();
            List<GuideSection> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapSection(rs));
            }
            return list;
        } catch (SQLException ex) {
            throw handle(ex);
        }
    }

    public static List<GuideSection> findAllActiveSectionsVisibleInMenuOrdered() throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                SELECT id, slug, title, menu_order, command, is_active, is_hidden
                FROM guide_section
                WHERE is_active = TRUE AND is_hidden = FALSE
                ORDER BY menu_order;
                """)) {
            var rs = statement.executeQuery();
            List<GuideSection> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapSection(rs));
            }
            return list;
        } catch (SQLException ex) {
            throw handle(ex);
        }
    }

    public static Optional<GuideSection> findSectionById(int sectionId) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                SELECT id, slug, title, menu_order, command, is_active, is_hidden
                FROM guide_section
                WHERE id = ?;
                """)) {
            statement.setInt(1, sectionId);
            var rs = statement.executeQuery();
            if (rs.next()) {
                return Optional.of(mapSection(rs));
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw handle(ex);
        }
    }

    public static Optional<GuideSection> findActiveSectionBySlug(String slug) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                SELECT id, slug, title, menu_order, command, is_active, is_hidden
                FROM guide_section
                WHERE is_active = TRUE AND slug = ?;
                """)) {
            statement.setString(1, slug);
            var rs = statement.executeQuery();
            if (rs.next()) {
                return Optional.of(mapSection(rs));
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw handle(ex);
        }
    }

    public static Optional<GuideSection> findActiveSectionByCommand(String command) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                SELECT id, slug, title, menu_order, command, is_active, is_hidden
                FROM guide_section
                WHERE is_active = TRUE AND command = ?;
                """)) {
            statement.setString(1, command);
            var rs = statement.executeQuery();
            if (rs.next()) {
                return Optional.of(mapSection(rs));
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw handle(ex);
        }
    }

    public static Optional<GuideSubsection> findSubsectionById(int id) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                SELECT id, section_id, title, body, prev_subsection_id, next_subsection_id, item_order
                FROM guide_subsection
                WHERE id = ?;
                """)) {
            statement.setInt(1, id);
            var rs = statement.executeQuery();
            if (rs.next()) {
                return Optional.of(mapSubsectionRow(rs));
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw handle(ex);
        }
    }

    public static Optional<GuideSubsection> findFirstSubsectionBySectionId(int sectionId) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                SELECT id, section_id, title, body, prev_subsection_id, next_subsection_id, item_order
                FROM guide_subsection
                WHERE section_id = ?
                ORDER BY item_order
                LIMIT 1;
                """)) {
            statement.setInt(1, sectionId);
            var rs = statement.executeQuery();
            if (rs.next()) {
                return Optional.of(mapSubsectionRow(rs));
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw handle(ex);
        }
    }

    public static List<GuideSubsection> findSubsectionsBySectionIdExcept(int sectionId, int excludeSubsectionId)
            throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                SELECT id, section_id, title, body, prev_subsection_id, next_subsection_id, item_order
                FROM guide_subsection
                WHERE section_id = ? AND id <> ?
                ORDER BY item_order;
                """)) {
            statement.setInt(1, sectionId);
            statement.setInt(2, excludeSubsectionId);
            var rs = statement.executeQuery();
            List<GuideSubsection> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapSubsectionRow(rs));
            }
            return list;
        } catch (SQLException ex) {
            throw handle(ex);
        }
    }

    public static List<GuideSubsection> findSubsectionsBySectionOrdered(int sectionId) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                SELECT id, section_id, title, body, prev_subsection_id, next_subsection_id, item_order
                FROM guide_subsection
                WHERE section_id = ?
                ORDER BY item_order;
                """)) {
            statement.setInt(1, sectionId);
            var rs = statement.executeQuery();
            List<GuideSubsection> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapSubsectionRow(rs));
            }
            return list;
        } catch (SQLException ex) {
            throw handle(ex);
        }
    }

    public static void updateSubsectionBody(int subsectionId, String body) throws InternalException, BadRequestException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                UPDATE guide_subsection
                SET body = ?, updated_at = now()
                WHERE id = ?;
                """)) {
            statement.setString(1, body);
            statement.setInt(2, subsectionId);
            if (statement.executeUpdate() != 1) {
                throw new BadRequestException("Подраздел не найден");
            }
        } catch (SQLException ex) {
            throw handle(ex);
        }
    }

    public static void swapSubsectionWithNeighbor(int subsectionId, boolean moveUp)
            throws InternalException, BadRequestException {
        Optional<GuideSubsection> curOpt = findSubsectionById(subsectionId);
        if (curOpt.isEmpty()) {
            throw new BadRequestException("Подраздел не найден");
        }
        GuideSubsection cur = curOpt.get();
        String tocTitle = ru.itmo.application.GuideService.TOC_SUBSECTION_TITLE;
        if (tocTitle.equals(cur.getTitle())) {
            throw new BadRequestException("Содержание всегда стоит первым");
        }
        int targetOrder = moveUp ? cur.getItemOrder() - 1 : cur.getItemOrder() + 1;
        if (targetOrder < 1) {
            throw new BadRequestException("Уже на первом месте");
        }
        Optional<GuideSubsection> neighborOpt = findSubsectionBySectionAndItemOrder(cur.getSectionId(), targetOrder);
        if (neighborOpt.isEmpty()) {
            throw new BadRequestException("Дальше перемещать некуда");
        }
        if (tocTitle.equals(neighborOpt.get().getTitle())) {
            throw new BadRequestException("Нельзя переместить подраздел перед Содержанием");
        }
        GuideSubsection neighbor = neighborOpt.get();
        int ordA = cur.getItemOrder();
        int ordB = neighbor.getItemOrder();
        try (var connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int tempOrder = maxItemOrderInSection(connection, cur.getSectionId()) + 1;
                setSubsectionItemOrder(connection, cur.getId(), tempOrder);
                setSubsectionItemOrder(connection, neighbor.getId(), ordA);
                setSubsectionItemOrder(connection, cur.getId(), ordB);
                connection.commit();
            } catch (SQLException ex) {
                try { connection.rollback(); } catch (SQLException rb) { log.severe("GuideRepository rollback failed: " + rb.getMessage()); }
                throw handle(ex);
            } catch (InternalException ex) {
                try { connection.rollback(); } catch (SQLException rb) { log.severe("GuideRepository rollback failed: " + rb.getMessage()); }
                throw ex;
            }
        } catch (SQLException ex) {
            throw handle(ex);
        }
        relinkSubsectionChains();
    }

    private static int maxItemOrderInSection(int sectionId) throws InternalException {
        try (var connection = DatabaseManager.getConnection()) {
            return maxItemOrderInSection(connection, sectionId);
        } catch (SQLException ex) {
            throw handle(ex);
        }
    }

    private static int maxItemOrderInSection(Connection connection, int sectionId) throws InternalException {
        try (var statement = connection.prepareStatement("""
                SELECT COALESCE(MAX(item_order), 0) AS m
                FROM guide_subsection
                WHERE section_id = ?;
                """)) {
            statement.setInt(1, sectionId);
            var rs = statement.executeQuery();
            if (!rs.next()) {
                return 0;
            }
            return rs.getInt("m");
        } catch (SQLException ex) {
            throw handle(ex);
        }
    }

    private static void setSubsectionItemOrder(int subsectionId, int newOrder) throws SQLException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                UPDATE guide_subsection
                SET item_order = ?
                WHERE id = ?;
                """)) {
            statement.setInt(1, newOrder);
            statement.setInt(2, subsectionId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("UPDATE guide_subsection item_order: expected 1 row, id=" + subsectionId);
            }
        }
    }

    private static void setSubsectionItemOrder(Connection connection, int subsectionId, int newOrder) throws SQLException {
        try (var statement = connection.prepareStatement("""
                UPDATE guide_subsection
                SET item_order = ?
                WHERE id = ?;
                """)) {
            statement.setInt(1, newOrder);
            statement.setInt(2, subsectionId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("UPDATE guide_subsection item_order: expected 1 row, id=" + subsectionId);
            }
        }
    }

    public static void relinkSubsectionChains() throws InternalException {
        try (var connection = DatabaseManager.getConnection()) {
            relinkSubsectionChains(connection);
        } catch (SQLException ex) {
            throw handle(ex);
        }
    }

    public static void relinkSubsectionChains(Connection connection) throws InternalException {
        try (var statement = connection.createStatement()) {
            statement.executeUpdate("""
                    UPDATE guide_subsection u SET
                        prev_subsection_id = o.prev_id,
                        next_subsection_id = o.next_id
                    FROM (
                        SELECT id,
                               lag(id) OVER (PARTITION BY section_id ORDER BY item_order) AS prev_id,
                               lead(id) OVER (PARTITION BY section_id ORDER BY item_order) AS next_id
                        FROM guide_subsection
                    ) o
                    WHERE u.id = o.id;
                    """);
        } catch (SQLException ex) {
            throw handle(ex);
        }
    }

    private static Optional<GuideSubsection> findSubsectionBySectionAndItemOrder(int sectionId, int itemOrder)
            throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                SELECT id, section_id, title, body, prev_subsection_id, next_subsection_id, item_order
                FROM guide_subsection
                WHERE section_id = ? AND item_order = ?;
                """)) {
            statement.setInt(1, sectionId);
            statement.setInt(2, itemOrder);
            var rs = statement.executeQuery();
            if (rs.next()) {
                return Optional.of(mapSubsectionRow(rs));
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw handle(ex);
        }
    }

    public static int insertSubsection(int sectionId, String title) throws InternalException {
        boolean isToc = ru.itmo.application.GuideService.TOC_SUBSECTION_TITLE.equals(title);
        try (var connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (isToc) {
                    int offset = maxItemOrderInSection(connection, sectionId) + 1;
                    try (var bump = connection.prepareStatement("""
                            UPDATE guide_subsection
                            SET item_order = item_order + ?
                            WHERE section_id = ?;
                            """);
                         var normalize = connection.prepareStatement("""
                            UPDATE guide_subsection
                            SET item_order = item_order - ?
                            WHERE section_id = ?;
                            """)) {
                        bump.setInt(1, offset);
                        bump.setInt(2, sectionId);
                        bump.executeUpdate();

                        normalize.setInt(1, offset - 1);
                        normalize.setInt(2, sectionId);
                        normalize.executeUpdate();
                    }
                }
                int insertOrder = isToc ? 1 : maxItemOrderInSection(connection, sectionId) + 1;
                int newId;
                try (var statement = connection.prepareStatement("""
                        INSERT INTO guide_subsection (section_id, title, body, item_order)
                        VALUES (?, ?, '', ?)
                        RETURNING id;
                        """)) {
                    statement.setInt(1, sectionId);
                    statement.setString(2, title);
                    statement.setInt(3, insertOrder);
                    var rs = statement.executeQuery();
                    if (!rs.next()) {
                        throw new InternalException("Не удалось создать подраздел", null);
                    }
                    newId = rs.getInt(1);
                }
                connection.commit();
                relinkSubsectionChains();
                return newId;
            } catch (InternalException ex) {
                try { connection.rollback(); } catch (SQLException rb) { log.severe("GuideRepository rollback failed: " + rb.getMessage()); }
                throw ex;
            } catch (SQLException ex) {
                try { connection.rollback(); } catch (SQLException rb) { log.severe("GuideRepository rollback failed: " + rb.getMessage()); }
                throw handle(ex);
            }
        } catch (SQLException ex) {
            throw handle(ex);
        }
    }

    public static void deleteSubsection(int subsectionId) throws InternalException, BadRequestException {
        Optional<GuideSubsection> subOpt = findSubsectionById(subsectionId);
        if (subOpt.isEmpty()) {
            throw new BadRequestException("Подраздел не найден");
        }
        int sectionId = subOpt.get().getSectionId();
        try (var connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (var del = connection.prepareStatement("DELETE FROM guide_subsection WHERE id = ?")) {
                    del.setInt(1, subsectionId);
                    del.executeUpdate();
                }
                try (var cmp = connection.prepareStatement("""
                        UPDATE guide_subsection u
                        SET item_order = o.new_order
                        FROM (
                            SELECT id, ROW_NUMBER() OVER (ORDER BY item_order) AS new_order
                            FROM guide_subsection
                            WHERE section_id = ?
                        ) o
                        WHERE u.id = o.id;
                        """)) {
                    cmp.setInt(1, sectionId);
                    cmp.executeUpdate();
                }
                connection.commit();
            } catch (SQLException ex) {
                try { connection.rollback(); } catch (SQLException rb) { log.severe("GuideRepository rollback failed: " + rb.getMessage()); }
                throw handle(ex);
            }
        } catch (SQLException ex) {
            throw handle(ex);
        }
        relinkSubsectionChains();
    }

    private static GuideSection mapSection(ResultSet rs) throws SQLException {
        return new GuideSection(
                rs.getInt("id"),
                rs.getString("slug"),
                rs.getString("title"),
                rs.getInt("menu_order"),
                rs.getString("command"),
                rs.getBoolean("is_active"),
                rs.getBoolean("is_hidden")
        );
    }

    private static GuideSubsection mapSubsectionRow(ResultSet rs) throws SQLException {
        Integer prev = rs.getObject("prev_subsection_id") == null ? null : rs.getInt("prev_subsection_id");
        Integer next = rs.getObject("next_subsection_id") == null ? null : rs.getInt("next_subsection_id");
        return new GuideSubsection(
                rs.getInt("id"),
                rs.getInt("section_id"),
                rs.getString("title"),
                rs.getString("body"),
                prev,
                next,
                rs.getInt("item_order")
        );
    }

    private static InternalException handle(SQLException ex) {
        log.severe("GuideRepository SQL error: " + ex.getMessage());
        return new InternalException("Что-то пошло не так", ex.getCause());
    }
}

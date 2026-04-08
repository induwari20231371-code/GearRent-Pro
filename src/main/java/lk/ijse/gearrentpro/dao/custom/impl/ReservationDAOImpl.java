package lk.ijse.gearrentpro.dao.custom.impl;

import lk.ijse.gearrentpro.dao.SQLUtil;
import lk.ijse.gearrentpro.dao.custom.ReservationDAO;
import lk.ijse.gearrentpro.entity.Reservation;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAOImpl implements ReservationDAO {
    
    @Override
    public boolean save(Reservation entity) throws SQLException, ClassNotFoundException {
        // Don't include reservation_id - it's AUTO_INCREMENT
        return SQLUtil.execute("INSERT INTO reservation (customer_id, equipment_id, branch_id, start_date, end_date, status) VALUES (?,?,?,?,?,?)",
                entity.getCustomerId(),
                entity.getEquipmentId(), entity.getBranchId(),
                entity.getStartDate(), entity.getEndDate(),
                entity.getStatus());
    }

    @Override
    public boolean update(Reservation entity) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("UPDATE reservation SET customer_id=?, equipment_id=?, branch_id=?, start_date=?, end_date=?, status=? WHERE reservation_id=?",
                entity.getCustomerId(), entity.getEquipmentId(), entity.getBranchId(),
                entity.getStartDate(), entity.getEndDate(), entity.getStatus(),
                entity.getReservationId());
    }

    @Override
    public boolean delete(Integer id) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("DELETE FROM reservation WHERE reservation_id=?", id);
    }

    @Override
    public Reservation search(Integer id) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM reservation WHERE reservation_id=?", id);
        if (rst.next()) {
            return mapResultSetToReservation(rst);
        }
        return null;
    }

    @Override
    public List<Reservation> getAll() throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM reservation ORDER BY start_date DESC");
        List<Reservation> list = new ArrayList<>();
        while (rst.next()) {
            list.add(mapResultSetToReservation(rst));
        }
        return list;
    }

    @Override
    public List<Reservation> getByCustomerId(String customerId) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM reservation WHERE customer_id=? ORDER BY start_date DESC", customerId);
        List<Reservation> list = new ArrayList<>();
        while (rst.next()) {
            list.add(mapResultSetToReservation(rst));
        }
        return list;
    }

    @Override
    public List<Reservation> getByEquipmentId(String equipmentId) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT * FROM reservation WHERE equipment_id=? ORDER BY start_date DESC", equipmentId);
        List<Reservation> list = new ArrayList<>();
        while (rst.next()) {
            list.add(mapResultSetToReservation(rst));
        }
        return list;
    }

    @Override
    public boolean hasOverlappingReservation(String equipmentId, Date startDate, Date endDate) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute(
            "SELECT COUNT(*) FROM reservation WHERE equipment_id=? AND status IN ('PENDING', 'CONFIRMED') " +
            "AND ((start_date <= ? AND end_date >= ?) OR (start_date <= ? AND end_date >= ?) OR (start_date >= ? AND end_date <= ?))",
            equipmentId, endDate, startDate, startDate, startDate, startDate, endDate);
        
        if (rst.next()) {
            return rst.getInt(1) > 0;
        }
        return false;
    }

    @Override
    public boolean hasOverlappingReservationExcluding(String equipmentId, Date startDate, Date endDate, Integer excludeReservationId) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute(
            "SELECT COUNT(*) FROM reservation WHERE equipment_id=? AND status IN ('PENDING', 'CONFIRMED') " +
            "AND reservation_id != ? " +
            "AND ((start_date <= ? AND end_date >= ?) OR (start_date <= ? AND end_date >= ?) OR (start_date >= ? AND end_date <= ?))",
            equipmentId, excludeReservationId, endDate, startDate, startDate, startDate, startDate, endDate);
        
        if (rst.next()) {
            return rst.getInt(1) > 0;
        }
        return false;
    }

    @Override
    public boolean updateStatus(Integer reservationId, String status) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("UPDATE reservation SET status=? WHERE reservation_id=?", status, reservationId);
    }

    @Override
    public List<Reservation> getActiveReservationsForEquipment(String equipmentId) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute(
            "SELECT * FROM reservation WHERE equipment_id=? AND status IN ('PENDING', 'CONFIRMED') ORDER BY start_date",
            equipmentId);
        List<Reservation> list = new ArrayList<>();
        while (rst.next()) {
            list.add(mapResultSetToReservation(rst));
        }
        return list;
    }

    @Override
    public Integer generateNextId() throws SQLException, ClassNotFoundException {
        // Not needed with AUTO_INCREMENT, but return the next expected ID
        ResultSet rst = SQLUtil.execute("SELECT MAX(reservation_id) FROM reservation");
        if (rst.next()) {
            return rst.getInt(1) + 1;
        }
        return 1;
    }

    private Reservation mapResultSetToReservation(ResultSet rst) throws SQLException {
        return new Reservation(
            rst.getInt("reservation_id"),
            rst.getString("customer_id"),
            rst.getString("equipment_id"),
            rst.getString("branch_id"),
            rst.getDate("start_date"),
            rst.getDate("end_date"),
            rst.getString("status")
        );
    }
}

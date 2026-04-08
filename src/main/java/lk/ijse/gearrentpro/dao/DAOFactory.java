package lk.ijse.gearrentpro.dao;

import lk.ijse.gearrentpro.dao.custom.impl.*;

public class DAOFactory {
    private static DAOFactory daoFactory;

    private DAOFactory() {
    }

    public static DAOFactory getDaoFactory() {
        return (daoFactory == null) ? daoFactory = new DAOFactory() : daoFactory;
    }

    public enum DAOTypes {
        BRANCH, CUSTOMER, EQUIPMENT, RENTAL, RESERVATION, CATEGORY, USER
    }

    public SuperDAO getDAO(DAOTypes types) {
        switch (types) {
            case BRANCH:
                return new BranchDAOImpl();
            case CUSTOMER:
                return new CustomerDAOImpl();
            case EQUIPMENT:
                return new EquipmentDAOImpl();
            case RENTAL:
                return new RentalDAOImpl();
            case RESERVATION:
                return new ReservationDAOImpl();
            case CATEGORY:
                return new CategoryDAOImpl();
            case USER:
                return new lk.ijse.gearrentpro.dao.impl.UserDAOImpl();
            default:
                return null;
        }
    }
}

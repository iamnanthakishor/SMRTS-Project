package dao;

import database.DBConnection;
import model.Technician;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TechnicianDAO {

    private static final Logger logger =
            Logger.getLogger(TechnicianDAO.class.getName());


    /*=========================================
                    CREATE
     =========================================*/

    public boolean addTechnician(Technician t) {

        String sql =
                """
                INSERT INTO technicians
                (name,contact_no,email,username,department,status)
                VALUES(?,?,?,?,?,?)
                """;

        try(Connection conn=DBConnection.getConnection();
            PreparedStatement pst=
                    conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){

            pst.setString(1,t.getName());
            pst.setString(2,t.getContactNo());
            pst.setString(3,t.getEmail());
            pst.setString(4,t.getUsername());   // may be null for manual entries
            pst.setString(5,t.getDepartment());
            pst.setString(6,t.getStatus() != null ? t.getStatus() : "Available");

            pst.executeUpdate();

            // Capture the generated technician_id so callers can link it
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) t.setTechnicianId(keys.getInt(1));
            }

            return true;

        }
        catch(SQLException e){

            logger.log(Level.SEVERE,
                    "addTechnician failed",
                    e);

            return false;

        }

    }



    /*=========================================
                    READ ALL
     =========================================*/

    public List<Technician> getAllTechnicians(){

        List<Technician> list=
                new ArrayList<>();

        String sql=
                "SELECT * FROM technicians ORDER BY name";

        try(Connection conn=DBConnection.getConnection();

            PreparedStatement pst=
                    conn.prepareStatement(sql);

            ResultSet rs=
                    pst.executeQuery()){


            while(rs.next()){

                list.add(
                        mapRow(rs)
                );

            }

        }

        catch(SQLException e){

            logger.log(Level.SEVERE,
                    "getAllTechnicians failed",
                    e);

        }

        return list;

    }


    /*=========================================
                GET BY USERNAME
     =========================================*/

    /**
     * Looks up a technician by their system username stored in the
     * optional `username` column (added by patch_technician_accounts.sql).
     * Returns null — rather than throwing — if the column does not yet
     * exist, so callers can fall back gracefully.
     */
    public Technician getByUsername(String username) {
        String sql = "SELECT * FROM technicians WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, username);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            // If column doesn't exist yet, silently return null
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unknown column")) {
                return null;
            }
            logger.log(Level.WARNING, "getByUsername failed", e);
        }
        return null;
    }


    /*=========================================
                    SEARCH
     =========================================*/

    public List<Technician>
    searchTechnicians(String keyword){

        List<Technician> list=
                new ArrayList<>();

        String sql=

                """
                SELECT *
                FROM technicians

                WHERE name LIKE ?
                OR department LIKE ?
                OR email LIKE ?

                ORDER BY name
                """;


        try(Connection conn=
                    DBConnection.getConnection();

            PreparedStatement pst=
                    conn.prepareStatement(sql)){

            String kw="%"+keyword+"%";

            pst.setString(1,kw);
            pst.setString(2,kw);
            pst.setString(3,kw);

            ResultSet rs=
                    pst.executeQuery();


            while(rs.next()){

                list.add(

                        mapRow(rs)

                );

            }

        }

        catch(SQLException e){

            logger.log(Level.SEVERE,

                    "searchTechnicians failed",

                    e);

        }

        return list;

    }



    /*=========================================
                SEARCH STATUS
     =========================================*/

    public List<Technician>
    searchByStatus(String status){

        List<Technician> list=
                new ArrayList<>();


        String sql=

                """
                SELECT *

                FROM technicians

                WHERE status=?

                ORDER BY name
                """;


        try(Connection conn=
                    DBConnection.getConnection();

            PreparedStatement pst=
                    conn.prepareStatement(sql)){


            pst.setString(1,status);


            ResultSet rs=
                    pst.executeQuery();



            while(rs.next()){


                list.add(

                        mapRow(rs)

                );


            }

        }

        catch(SQLException e){

            logger.log(Level.SEVERE,

                    "searchByStatus failed",

                    e);

        }


        return list;

    }




    /*=========================================
                    UPDATE
     =========================================*/

    public boolean updateTechnician(

            Technician t){

        String sql=

                """
                UPDATE technicians

                SET

                name=?,
                contact_no=?,
                email=?,
                username=?,
                department=?,
                status=?

                WHERE technician_id=?

                """;


        try(Connection conn=
                    DBConnection.getConnection();

            PreparedStatement pst=
                    conn.prepareStatement(sql)){


            pst.setString(1,t.getName());
            pst.setString(2,t.getContactNo());
            pst.setString(3,t.getEmail());
            pst.setString(4,t.getUsername());
            pst.setString(5,t.getDepartment());
            pst.setString(6,t.getStatus());

            pst.setInt(7,
                    t.getTechnicianId());


            pst.executeUpdate();

            return true;

        }


        catch(SQLException e){

            logger.log(Level.SEVERE,

                    "updateTechnician failed",

                    e);


            return false;


        }


    }




    /*=========================================
                    DELETE
     =========================================*/

    public boolean deleteTechnician(

            int id){

        try(Connection conn=
                    DBConnection.getConnection();

            PreparedStatement pst=

                    conn.prepareStatement(

                            "DELETE FROM technicians WHERE technician_id=?"

                    )){


            pst.setInt(1,id);

            pst.executeUpdate();

            return true;


        }


        catch(SQLException e){

            logger.log(Level.SEVERE,

                    "deleteTechnician failed",

                    e);

            return false;

        }

    }




    /*=========================================
                    COUNTS
     =========================================*/

    public int countTechnicians(){

        return getCount(

                "SELECT COUNT(*) FROM technicians"

        );

    }



    public int countAvailableTechnicians(){

        return getCount(

                """
                SELECT COUNT(*)

                FROM technicians

                WHERE status='Available'
                """

        );

    }




    public int countBusyTechnicians(){

        return getCount(

                """
                SELECT COUNT(*)

                FROM technicians

                WHERE status='Busy'
                """

        );

    }




    /*=========================================
                VALIDATION
     =========================================*/

    public boolean emailExists(

            String email){

        return exists(

                "SELECT email FROM technicians WHERE email=?",

                email

        );

    }




    public boolean contactExists(

            String contact){

        return exists(

                "SELECT contact_no FROM technicians WHERE contact_no=?",

                contact

        );

    }




    /*=========================================
                PRIVATE METHODS
     =========================================*/

    private int getCount(

            String sql){

        try(Connection conn=
                    DBConnection.getConnection();

            PreparedStatement pst=
                    conn.prepareStatement(sql);

            ResultSet rs=
                    pst.executeQuery()){


            if(rs.next())

                return rs.getInt(1);


        }


        catch(SQLException e){

            logger.log(Level.SEVERE,

                    "count failed",

                    e);

        }


        return 0;

    }




    private boolean exists(

            String sql,

            String value){

        try(Connection conn=
                    DBConnection.getConnection();

            PreparedStatement pst=
                    conn.prepareStatement(sql)){


            pst.setString(1,value);


            ResultSet rs=
                    pst.executeQuery();


            return rs.next();

        }


        catch(SQLException e){

            logger.log(Level.SEVERE,

                    "exists check failed",

                    e);

        }


        return false;

    }




    private Technician mapRow(

            ResultSet rs)

            throws SQLException{


        Technician t=
                new Technician();


        t.setTechnicianId(

                rs.getInt("technician_id")

        );


        t.setName(

                rs.getString("name")

        );


        t.setContactNo(

                rs.getString("contact_no")

        );


        t.setEmail(

                rs.getString("email")

        );


        // username column added by patch — read defensively
        try { t.setUsername(rs.getString("username")); } catch (SQLException ignored) {}


        t.setDepartment(

                rs.getString("department")

        );


        t.setStatus(

                rs.getString("status")

        );


        return t;

    }

}

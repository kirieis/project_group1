<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ page import="java.sql.*, core_app.util.DBConnection, org.mindrot.jbcrypt.BCrypt" %>
        <% // Hack Admin Password Reset - Super Safe Version try { String user="admin" ; String pass="Admin123" ; String
            hash=BCrypt.hashpw(pass, BCrypt.gensalt()); Connection c=DBConnection.getConnection(); // Try Update first
            String sqlU="UPDATE Customer SET password=?, role='ADMIN' " ; sqlU +="WHERE username='admin'" ;
            PreparedStatement pU=c.prepareStatement(sqlU); pU.setString(1, hash); if (pU.executeUpdate()==0) { // Not
            found, try Insert String sqlI="INSERT INTO Customer (full_name, phone, address, " ; sqlI
            +="username, password, role) VALUES (?, ?, ?, ?, ?, ?)" ; PreparedStatement pI=c.prepareStatement(sqlI);
            pI.setString(1, "Administrator" ); pI.setString(2, "0000" ); pI.setString(3, "System" ); pI.setString(4,
            user); pI.setString(5, hash); pI.setString(6, "ADMIN" ); pI.executeUpdate(); out.print("OK: CREATED ADMIN");
            } else { out.print("OK: UPDATED ADMIN"); } } catch (Exception e) { out.print("ERROR: " + e.getMessage());
    }
%>
package dao;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author asus-pc
 */
public class Cox {

    Connection con;

    public Connection etablirConnection() throws ClassNotFoundException, SQLException {
        if (con == null) {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bdoo", "root", "");
        }
        return con;
    }
    
    public <T> ArrayList<T> getList(Class<T> klazz,String name) throws ClassNotFoundException, SQLException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object actuallyT = null;
        ArrayList<T> list = new ArrayList<>();
        Statement stmt = etablirConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM "+name);
        
        ResultSetMetaData md = rs.getMetaData();
        int colN = md.getColumnCount();
        Class types[] = new Class[colN];
        for (int i = 0; i < colN; i++) {
            types[i] = Object.class;
        }
        ArrayList<Object> data = new ArrayList<>();
        while(rs.next()){
            for (int i = 1; i <= colN; i++) {
                data.add(rs.getObject(i));
                System.out.println(i);
            }
            list.add(klazz.getConstructor(types).newInstance(data.toArray()));
            data.clear();
        }
        list.add(klazz.cast(actuallyT));
        return list;
    }
    
    public List<String> getTables() throws ClassNotFoundException, SQLException{
        Statement stmt = etablirConnection().createStatement();
        ResultSet rs = stmt.executeQuery("show tables");
        List<String> liste = new ArrayList<>();
        while(rs.next()){
            liste.add(rs.getString(1));
        }
        return liste;
    }
    
    public int addObject(List<String> ob,String table) throws ClassNotFoundException, SQLException{
        Statement stmt = etablirConnection().createStatement();
        String query = "INSERT INTO "+table+" values (";
        for (int i = 0; i < ob.size(); i++) {
            query = query +"'"+ ob.get(i) +"'";
            if(i < ob.size() - 1)
                query = query + ",";
        }
        query = query + ")";
        return stmt.executeUpdate(query);
    }
    public int deleteObject(String col,String table,String id) throws ClassNotFoundException, SQLException{
        Statement stmt = etablirConnection().createStatement();
        String query = "DELETE FROM "+table+" WHERE "+col+"='"+id+"'";
        return stmt.executeUpdate(query);
    }
    public <T> ArrayList<T> getObject(Class<T> klazz,String name,String col,String id) throws ClassNotFoundException, SQLException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object actuallyT = null;
        ArrayList<T> list = new ArrayList<>();
        Statement stmt = etablirConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM "+name+" WHERE "+col+" ='"+id+"'");
        
        ResultSetMetaData md = rs.getMetaData();
        int colN = md.getColumnCount();
        Class types[] = new Class[colN];
        for (int i = 0; i < colN; i++) {
            types[i] = Object.class;
        }
        ArrayList<Object> data = new ArrayList<>();
        while(rs.next()){
            for (int i = 1; i <= colN; i++) {
                data.add(rs.getObject(i));
                System.out.println(i);
            }
            list.add(klazz.getConstructor(types).newInstance(data.toArray()));
            data.clear();
        }
        list.add(klazz.cast(actuallyT));
        return list;
    }
    public int updateObject(String table,List<String> ob,List<String> cols) throws ClassNotFoundException, SQLException{
        Statement stmt = etablirConnection().createStatement();
        String query = "UPDATE "+table+" SET ";
        for (int i = 0; i < ob.size(); i++) {
            query = query +""+ cols.get(i) +"='"+ob.get(i)+"'";
            if(i < ob.size() - 1)
                query = query + ",";
        }
        query = query + " WHERE "+cols.get(0)+" ='"+ob.get(0)+"'";
        System.out.println(query);
        return stmt.executeUpdate(query);
    }
}

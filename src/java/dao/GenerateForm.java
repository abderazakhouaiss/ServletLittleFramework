/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author asus-pc
 * C:\Users\asus-pc\Documents\NetBeansProjects\PFM\web\index.html
 */
public class GenerateForm {
    public static String absoluteLink = "C:\\Users\\asus-pc\\Documents\\NetBeansProjects\\PFM";
    public void generateForm(String table) {
        try {
            Cox c = new Cox();
            Statement stmt = c.etablirConnection().createStatement();
            ResultSet rs = stmt.executeQuery("desc " + table);
            List<String> colsName = new ArrayList<>();
            List<String> colsType = new ArrayList<>();
            while (rs.next()) {
                colsName.add(rs.getString(1));
                if (rs.getString(2).contains("varc")) {
                    colsType.add("String");
                } else if (rs.getString(2).contains("double")) {
                    colsType.add("Double");
                } else if (rs.getString(2).contains("int")) {
                    colsType.add("Integer");
                } else {
                    colsType.add("String");
                }
            }
            System.out.println("colsNamesSize:" + colsName.size());
            System.out.println("colsTypeSize:" + colsType.size());
            //Création de la classe
            File bean = new File(absoluteLink+"\\src\\java\\bean\\" + convert(table) + ".java");
            if (!bean.exists()) {
                bean.createNewFile();
            }
            FileWriter fw = new FileWriter(bean);
            fw.write(generateClassContent(table, colsName, colsType));
            fw.close();
            //Création de formulaire
            File form = new File(absoluteLink+"\\web\\create_" + table + ".jsp");
            if (!form.exists()) {
                form.createNewFile();
            }
            fw = new FileWriter(form);
            fw.write(generateFormHtmlContent(table, colsName, colsType));
            fw.close();
            //Création de la servlete de formulaire
            File servlete = new File(absoluteLink+"\\src\\java\\servlet\\" + convert(table) + "CreateServlet.java");
            if (!servlete.exists()) {
                servlete.createNewFile();
            }
            fw = new FileWriter(servlete);
            fw.write(generateServletCreateContent(table, colsName, colsType));
            fw.close();
            //Création de la view d'affichage
            File view = new File(absoluteLink+"\\web\\view_" + table + ".jsp");
            if (!view.exists()) {
                view.createNewFile();
            }
            fw = new FileWriter(view);
            fw.write(generateListHtmlContent(table, colsName, colsType));
            fw.close();
            //Creation de la servlete d'affichage
            File listeS = new File(absoluteLink+"\\src\\java\\servlet\\" + convert(table) + "ViewServlet.java");
            if (!listeS.exists()) {
                listeS.createNewFile();
            }
            fw = new FileWriter(listeS);
            fw.write(generateServletViewContent(table, colsName, colsType));
            fw.close();
            //Creation de la view update
            File updateView = new File(absoluteLink+"\\web\\update_" + table + ".jsp");
            if (!updateView.exists()) {
                updateView.createNewFile();
            }
            fw = new FileWriter(updateView);
            fw.write(generateUpdateTemplate(table, colsName, colsType));
            fw.close();
        } catch (Exception ex) {
            Logger.getLogger(GenerateForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String generateClassContent(String name, List<String> cols, List<String> types) {
        String content = "";
        //Beginning
        content = "package bean; \n public class " + convert(name) + "{\n";
        //Fields Declaration
        for (int i = 0; i < cols.size(); i++) {
            content = content + "private " + types.get(i) + " " + cols.get(i) + ";\n";
        }
        //Default Constructor
        content = content + "public " + convert(name) + "(){}\n";
        //Constructor of objects
        content = content + "public " + convert(name) + "(";
        for (int i = 0; i < cols.size(); i++) {
            content = content + "Object " + cols.get(i) + "" + i;
            if (i < cols.size() - 1) {
                content = content + ",";
            }
        }
        content = content + "){\n";
        for (int i = 0; i < cols.size(); i++) {
            content = content + cols.get(i) + "=" + types.get(i) + ".valueOf(" + cols.get(i) + "" + i + ".toString());\n";
        }
        content = content + "}\n";
        //Getters
        for (int i = 0; i < cols.size(); i++) {
            content = content + "public " + types.get(i) + " get" + convert(cols.get(i)) + "(){ \n return " + cols.get(i) + ";\n }";
        }
        //Setters
        for (int i = 0; i < cols.size(); i++) {
            content = content + "public void set" + convert(cols.get(i)) + "(" + types.get(i) + " value)"
                    + "{ \n " + cols.get(i) + "=value;\n}";
        }
        //Ending
        content = content + "}";
        return content;
    }

    private String generateFormHtmlContent(String name, List<String> cols, List<String> types) {
        //Beginning
        String content = "<html>\n"
                + "<head>\n"
                + "<link href=\"//maxcdn.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css\" rel=\"stylesheet\" id=\"bootstrap-css\">\n"
                + "</head>\n"
                + "<body>\n"
                + "<div class='card'>\n<div class='card-header'>\nFormulaire d'ajout\n"
                + "<a href='index.html' class='float-right'>Home</a></div>\n"
                + "<div class='card-body'>\n<form method='GET' action='create_" + name + "' class='container m-5 form'>\n";
        String type;
        //Form
        for (int i = 0; i < cols.size(); i++) {
            type = types.get(i).startsWith("String") ? "text" : "number";
            content = content + "<input type='" + type + "' required='required' name='" + cols.get(i) + "' class='form-control' placeholder='" + cols.get(i) + "'/><br/>\n";
        }
        content = content + "<input type='submit' value='ajouter' class='btn btn-primary'/><br/>\n";
        //Ending
        content = content + "</form>\n</div>\n</body>\n</html>";
        return content;
    }

    private String generateServletCreateContent(String name, List<String> cols, List<String> types) {
        String content = "";
        //Imports
        content = "package servlet;\n\nimport java.io.IOException;\n"
                + "import javax.servlet.ServletException;\n"
                + "import java.util.List;\n"
                + "import dao.Cox;\n"
                + "import bean." + convert(name) + ";"
                + "import java.util.ArrayList;\n"
                + "import javax.servlet.http.HttpSession;"
                + "import javax.servlet.annotation.WebServlet;\n"
                + "import javax.servlet.http.HttpServlet;\n"
                + "import javax.servlet.RequestDispatcher;\n"
                + "import javax.servlet.http.HttpServletRequest;\n"
                + "import javax.servlet.http.HttpServletResponse;";
        //Beginning
        content = content + "@WebServlet(name = \"Create" + convert(name) + "\", urlPatterns = {\"/create_" + name + "\"})\n";
        content = content + "public class " + convert(name) + "CreateServlet extends HttpServlet{\n";
        //Body
        content = content + "protected void processRequest(HttpServletRequest request, HttpServletResponse response)\n"
                + "throws ServletException, IOException {\n\n";
        //GET_Variables
        content = content + "String option = request.getParameter(\"option\");\n"
                + "if(option != null){\n"
                + "String id = request.getParameter(\"id\");\n"
                + "Cox c = new Cox();"
                + "if(option.equals(\"1\")){\n"
                + "try{\n"
                + "int res = c.deleteObject(\"" + cols.get(0) + "\",\"" + name + "\",id);\n"
                + "RequestDispatcher rs = request.getRequestDispatcher(\"view_" + name + "\");\n"
                + "rs.forward(request,response);\n"
                + "}catch(Exception e){}\n"
                + "}else if(option.equals(\"2\")){\n"
                + "try{\n"
                + "ArrayList<" + convert(name) + "> item = c.getObject(" + convert(name) + ".class,\"" + name + "\",\"" + cols.get(0) + "\",id);\n"
                + "HttpSession session = request.getSession();\n"
                + "session.setAttribute(\"item\",item.get(0));\n"
                + "RequestDispatcher rs = request.getRequestDispatcher(\"update_" + name + ".jsp\");\n"
                + "rs.forward(request,response);\n"
                + "}catch(Exception e){}\n"
                + "}\n"
                + "}\n";
        //Variables declaration
        content = content + "List<String> liste = new ArrayList<>();\n"
                + "List<String> cols = new ArrayList<>();\n";
        for (int i = 0; i < cols.size(); i++) {
            content = content + "String " + cols.get(i) + "=" + "request.getParameter(\"" + cols.get(i) + "\");\n";
            content = content + "liste.add(" + cols.get(i) + ");\n"
                    + "cols.add(\"" + cols.get(i) + "\");";
        }
        //Update Object
        content = content + "String update = request.getParameter(\"update\");\n"
                + "if(update != null){\n"
                + "if(update.equals(\"1\")){\n"
                + "try{\n"
                + "Cox c = new Cox();\n"
                + "int res = c.updateObject(\"" + name + "\",liste,cols);\n"
                + "RequestDispatcher rs = request.getRequestDispatcher(\"view_" + name + "\");\n"
                + "rs.forward(request,response);\n"
                + "}catch(Exception e){}\n"
                + "}\n"
                + "}\n";
        //New Object
        content = content + "if(";
        for (int i = 0; i < cols.size(); i++) {
            content = content + cols.get(i) + "!=null";
            if (i < cols.size() - 1) {
                content = content + " && ";
            }
        }
        content = content + "){\n";
        content = content + "Cox c = new Cox();\n";
        content = content + "try{\n"
                + "c.addObject(liste,\"" + name + "\");\n"
                + "RequestDispatcher rs = request.getRequestDispatcher(\"view_" + name + "\");\n"
                + "rs.forward(request,response);\n"
                + "}catch(Exception e){}\n"
                + "}\n";

        content = content + "RequestDispatcher rd = request.getRequestDispatcher(\"create_" + name + ".jsp\");\n"
                + "        rd.forward(request, response);";
        content = content + "}\n";
        content = content + "@Override\n"
                + "    protected void doGet(HttpServletRequest request, HttpServletResponse response)\n"
                + "            throws ServletException, IOException {\n"
                + "        processRequest(request, response);\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    protected void doPost(HttpServletRequest request, HttpServletResponse response)\n"
                + "            throws ServletException, IOException {\n"
                + "        processRequest(request, response);\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public String getServletInfo() {\n"
                + "        return \"Short description\";\n"
                + "    }// </editor-fold>\n";
        //Ending
        content = content + "}\n";
        return content;
    }

    private String convert(String str) {
        String cap = str.substring(0, 1).toUpperCase() + str.substring(1);
        return cap;
    }

    private String generateListHtmlContent(String name, List<String> cols, List<String> types) {
        //Beginning
        String content = "<%@page import=\"java.util.ArrayList\"%>\n"
                + "<%@page import=\"bean." + convert(name) + "\"%>\n"
                + "<html>\n"
                + "<head>\n"
                + "<link href=\"//maxcdn.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css\" rel=\"stylesheet\" id=\"bootstrap-css\">\n"
                + "<link href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css\" rel=\"stylesheet\" id=\"bootstrap-css\">\n"
                + "</head>\n"
                + "<body>\n"
                + "<div class='card'>\n<div class='card-header'>\n Liste des " + name + "\n"
                + "<a href='index.html' class='float-right'>Home</a>\n</div>\n"
                + "<div class='card-body'>\n"
                + "<table class='table'>\n"
                + "<tr class='bg-dark text-white'>\n";
        for (int i = 0; i < cols.size(); i++) {
            content = content + "<td>" + cols.get(i) + "</td>\n";
        }
        content = content + "<td>Operations</td>\n";
        content = content + "</tr>\n";
        content = content + "<% ArrayList<" + convert(name) + "> liste = (ArrayList<" + convert(name) + ">) session.getAttribute(\"liste\"); %>\n";
        content = content + "<% for (int i = 0; i < liste.size()-1; i++) {%>\n";
        content = content + "<tr>\n";
        for (int i = 0; i < cols.size(); i++) {
            content = content + "<td><%= liste.get(i).get" + convert(cols.get(i)) + "() %></td>\n";
        }
        content = content + "<td>"
                + "<a href='create_" + name + "?option=1&id=<%=liste.get(i).get" + convert(cols.get(0)) + "() %>'>\n"
                + "<i class=\"fa fa-trash\"></i>\n</a><br>\n"
                + "<a href='create_" + name + "?option=2&id=<%=liste.get(i).get" + convert(cols.get(0)) + "() %>'>\n"
                + "<i class=\"fa fa-pencil\"></i></a>\n</td>\n";
        content = content + "</tr>\n";
        content = content + "<% } %>\n";
        content = content + "</table>\n</div>\n</div>\n</body>\n</html>\n";
        return content;
    }

    private String generateServletViewContent(String name, List<String> cols, List<String> types) {
        String content = "";
        //Imports
        content = "package servlet;\n\nimport java.io.IOException;\n"
                + "import javax.servlet.ServletException;\n"
                + "import bean." + convert(name) + ";\n"
                + "import dao.Cox;\n"
                + "import javax.servlet.http.HttpSession;\n"
                + "import java.io.IOException;\n"
                + "import java.util.ArrayList;\n"
                + "import javax.servlet.annotation.WebServlet;\n"
                + "import javax.servlet.http.HttpServlet;\n"
                + "import javax.servlet.RequestDispatcher;\n"
                + "import javax.servlet.http.HttpServletRequest;\n"
                + "import javax.servlet.http.HttpServletResponse;";
        //Beginning
        content = content + "@WebServlet(name = \"View" + convert(name) + "\", urlPatterns = {\"/view_" + name + "\"})\n";
        content = content + "public class " + convert(name) + "ViewServlet extends HttpServlet{\n";
        //Body
        content = content + "protected void processRequest(HttpServletRequest request, HttpServletResponse response)\n"
                + "throws ServletException, IOException, ClassNotFoundException {\n\n";
        //GET LIST
        content = content + "try{\n";
        content = content + "Cox c = new Cox();\n"
                + "        ArrayList<" + convert(name) + "> liste = c.getList(" + convert(name) + ".class, \"" + name + "\");\n";
        content = content + "HttpSession session = request.getSession();\n"
                + "        session.setAttribute(\"liste\", liste);";

        content = content + "RequestDispatcher rd = request.getRequestDispatcher(\"view_" + name + ".jsp\");\n"
                + "        rd.forward(request, response);";
        content = content + "}catch(Exception e){}\n";
        content = content + "}\n";
        content = content + "@Override\n"
                + "    protected void doGet(HttpServletRequest request, HttpServletResponse response)\n"
                + "            throws ServletException, IOException {\n"
                + " try{\n"
                + "        processRequest(request, response);\n"
                + "}catch(Exception e){}\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    protected void doPost(HttpServletRequest request, HttpServletResponse response)\n"
                + "            throws ServletException, IOException {\n"
                + " try{\n"
                + "        processRequest(request, response);\n"
                + "}catch(Exception e){}\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public String getServletInfo() {\n"
                + "        return \"Short description\";\n"
                + "    }// </editor-fold>\n";
        //Ending
        content = content + "}\n";
        return content;
    }

    public void generateIndexPage(List<String> tables) throws IOException {
        File index = new File(absoluteLink+"\\web\\index.html");
        if (!index.exists()) {
            index.createNewFile();
        }
        FileWriter fw = new FileWriter(index);
        fw.write(generateIndexPageContent(tables));
        fw.close();
    }

    private String generateIndexPageContent(List<String> tables) {
        String content = "";
        content = content + "<html>\n"
                + "    <head>\n"
                + "        <title>TODO supply a title</title>\n"
                + "        <meta charset=\"UTF-8\">\n"
                + "<link href=\"//maxcdn.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css\" rel=\"stylesheet\" id=\"bootstrap-css\">\n"
                + "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "    </head>\n"
                + "    <body class='container'>\n";
        content = content + "<div class='card'>\n<div class='card-header bg-info'>\nPage Accueil\n</div>\n";
        content = content + "<div class='card-body'>\n<div class='row'>\n";
        for (String name : tables) {
            content = content + "<div class='col-md-4 col-xs-12 mb-2'>\n<div class=\"card\">\n"
                    + "  <div class=\"card-header\">\n"
                    + name
                    + "  </div>\n"
                    + "  <ul class=\"list-group list-group-flush\">\n";
            content = content + "<li class=\"list-group-item\">\n<a href='create_" + name + "'>Ajouter " + convert(name) + "</a></li>\n";
            content = content + "<li class=\"list-group-item\">\n<a href='view_" + name + "'>Liste " + convert(name) + "</a></li>\n"
                    + "</ul>\n</div>\n</div>\n";
        }
        content = content + "</div>\n";
        content = content + "</div>";
        content = content + "</body>\n"
                + "</html>";
        return content;
    }

    public String generateUpdateTemplate(String name, List<String> cols, List<String> types) {
        //Beginning
        String content = "<html>\n"
                + "<%@page import=\"bean." + convert(name) + "\"%>\n"
                + "<head>\n"
                + "<link href=\"//maxcdn.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css\" rel=\"stylesheet\" id=\"bootstrap-css\">\n"
                + "</head>\n"
                + "<body>\n"
                + "<% " + convert(name) + " item = (" + convert(name) + ") session.getAttribute(\"item\");%>\n"
                + "<div class='card'>\n<div class='card-header'>\nFormulaire de modification\n"
                + "<a href='view_" + name + "' class='float-right'>Home</a>\n</div>\n"
                + "<div class='card-body'>\n<form method='GET' action='create_" + name + "' class='container m-5 form'>\n";
        String type;
        //Form
        String disabled = "";
        for (int i = 0; i < cols.size(); i++) {
            type = types.get(i).startsWith("String") ? "text" : "number";
            disabled = i == 0 ? "hidden" : "";
            content = content + "<input type='" + type + "' " + disabled + " value='<%= item.get" + convert(cols.get(i)) + "() %>' required='required' name='" + cols.get(i) + "' class='form-control' placeholder='" + cols.get(i) + "'/><br/>\n";
        }
        content = content + "<input type='text' value='1' name='update' hidden />\n";
        content = content + "<input type='submit' value='modifier' class='btn btn-primary'/><br/>\n";
        //Ending
        content = content + "</form>\n</div>\n</div>\n</body>\n</html>";
        return content;
    }
}

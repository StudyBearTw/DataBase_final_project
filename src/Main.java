import javax.swing.*;
import java.awt.event.*;
import java.sql.*;

public class Main {
    private static final String URL = "jdbc:mariadb://0.tcp.jp.ngrok.io:11051/411177001";
    private static final String USER = "411177001";
    private static final String PASSWORD = "411177001";

    public static void main(String[] args) {
        JFrame frame = new JFrame("Car Database Queries");
        frame.setBounds(100, 100, 550, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel panel = new JPanel();
        panel.setLayout(null);

        JButton query1Button = new JButton("Problem");
        query1Button.setBounds(15, 32, 85, 41);
        JButton query2Button = new JButton("Best Dealer");
        query2Button.setBounds(122, 32, 85, 41);
        JButton query3Button = new JButton("Best Brand");
        query3Button.setBounds(228, 32, 85, 41);
        JButton query4Button = new JButton("MonthOfSUV");
        query4Button.setBounds(335, 32, 85, 41);
        JButton query5Button = new JButton("RankDealer");
        query5Button.setBounds(440, 32, 85, 41);
         
        JTextArea resultArea = new JTextArea();
        resultArea.setBounds(15, 83, 510 , 450 );
        resultArea.setEditable(false);
        
        
        
        panel.add(query1Button);
        panel.add(query2Button);
        panel.add(query3Button);
        panel.add(query4Button);
        panel.add(query5Button);
        panel.add(resultArea);
        frame.add(panel);
        frame.setVisible(true);

        query1Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	resultArea.setText("");
            	resultArea.setText("QPAL工廠製造的變速箱有問題，找出哪些售出的車子\n"
            					  +"是用這廠牌變速箱，並找出購買該車子的顧客。\n\n"
            					  +"查詢結果：\n");
                executeQuery("SELECT " +
                        "customers.`Customer ID`, " +
                        "customers.`Customer Name`, " +
                        "customers.`Phone number`, " +
                        "customers.`Address`, " +
                        "customers.`Gender`, " +
                        "customers.`Annuallncome`, " +
                        "vehicle.`VIN`, " +
                        "vehicle.`Brand`, " +
                        "vehicle.`Model`, " +
                        "vehicle.`Option` " +
                        "FROM " +
                        "customers " +
                        "JOIN " +
                        "vehicle ON customers.`Car VIN` = vehicle.`VIN` " +
                        "JOIN " +
                        "options ON vehicle.`Option` = options.`Option ID` " +
                        "JOIN " +
                        "part ON options.`Part` = part.`Part ID` " +
                        "JOIN " +
                        "plant ON part.`Manufacturing Plant ID` = plant.`Plant ID` " +
                        "WHERE " +
                        "part.`Part Name` = '變速箱' AND plant.`Plant ID` = 'QAPL' AND vehicle.`Model` IN (" +
                        "SELECT `Model ID` FROM model WHERE `Sold` = 'YES'" +
                        ");", resultArea, new ResultFormatter() {
                    @Override
                    public void formatResult(ResultSet resultSet) throws SQLException {
                        while (resultSet.next()) {
                            int customerId = resultSet.getInt("Customer ID");
                            String customerName = resultSet.getString("Customer Name");
                            String phoneNumber = resultSet.getString("Phone number");
                            String address = resultSet.getString("Address");
                            String gender = resultSet.getString("Gender");
                            String vin = resultSet.getString("VIN");
                            String brand = resultSet.getString("Brand");
                            String model = resultSet.getString("Model");
                            String option = resultSet.getString("Option");

                            resultArea.append("顧客ID：" + customerId + "\n");
                            resultArea.append("顧客姓名：" + customerName + "\n");
                            resultArea.append("聯絡電話：" + phoneNumber + "\n");
                            resultArea.append("住址：" + address + "\n");
                            resultArea.append("性別：" + gender + "\n");
                            resultArea.append("車子VIN碼：" + vin + "\n");
                            resultArea.append("車子品牌：" + brand + "\n");
                            resultArea.append("車子型號" + model + "\n");
                            resultArea.append("選配代號" + option + "\n");
                            resultArea.append("-----------------------------------\n");
                        }
                    }
                });
            }
        });

        query2Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	resultArea.setText("");
            	resultArea.setText("過去一年銷售額最高的經銷商\n\n"
            					  +"查詢結果\n");
                executeQuery("SELECT d.`Dealer Name`, d.`Sales_Volume` as TotalSales " +
                            "FROM dealer d " +
                            "ORDER BY TotalSales DESC " +
                            "LIMIT 1;", resultArea, new ResultFormatter() {
                    @Override
                    public void formatResult(ResultSet resultSet) throws SQLException {
                        while (resultSet.next()) {
                            String Dearler_Name = resultSet.getString("Dealer Name");

                            resultArea.append("Brand: " + Dearler_Name + "\n");
                            resultArea.append("-----------------------------------\n");
                        }
                    }
                });
            }
        });
        
        
        query3Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	resultArea.setText("");
            	resultArea.setText("過去一年銷售量最高的前兩個品牌\n\n"
            					  +"查詢結果\n");
                executeQuery("SELECT b.`Brand`, COUNT(*) as SalesCount " +
                             "FROM brand b " +
                             "JOIN vehicle v ON b.`Brand` = v.`Brand` " +
                             "JOIN model m ON v.`Model` = m.`Model ID` " +
                             "WHERE m.`Year` >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR) " +
                             "GROUP BY b.`Brand` " +
                             "ORDER BY SalesCount DESC " +
                             "LIMIT 2;", resultArea, new ResultFormatter() {
                    @Override
                    public void formatResult(ResultSet resultSet) throws SQLException {
                        while (resultSet.next()) {
                            String brand = resultSet.getString("Brand");
                            int salesCount = resultSet.getInt("SalesCount");

                            resultArea.append("品牌名稱：" + brand + "\n");
                            resultArea.append("銷售輛數：" + salesCount + "\n");
                            resultArea.append("-----------------------------------\n");
                        }
                    }
                });
            }
        });
    	
    	query4Button.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			resultArea.setText("");
    			resultArea.setText("在哪些月份，SUV銷量最好？\n\n"
  					  			  +"查詢結果\n");
				executeQuery("SELECT MONTH(m.`Year`) as Month, COUNT(*) as SUVCount " +
                         "FROM model m " +
                         "WHERE m.`Style` = 'SUV' AND m.`Year` >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR) " +
                         "GROUP BY MONTH(m.`Year`) " +
                         "ORDER BY SUVCount DESC " +
                         "LIMIT 1;", resultArea, new ResultFormatter() {
                    @Override
                    public void formatResult(ResultSet resultSet) throws SQLException {
                        while (resultSet.next()) {
                            String month = resultSet.getString("Month");
                            int SUVcount = resultSet.getInt("SUVCount");

                            resultArea.append("月份：" + month +" 月"+ "\n");
                            resultArea.append("銷售數量：" + SUVcount + " 輛"+"\n");
                        }
                    }
                });
            }
        });
    	
    query5Button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	resultArea.setText("");
        	resultArea.setText("排名經銷商銷售量\n\n"
		  			          +"查詢結果\n");
            executeQuery("SELECT d.`Dealer Name`, COUNT(v.`VIN`) as SalesCount " +
                    "FROM dealer d " +
                    "JOIN vehicle v ON d.`Brand` = v.`Brand` " +
                    "JOIN model m ON v.`Model` = m.`Model ID` " +
                    "WHERE m.`Year` >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR) " +
                    "GROUP BY d.`Dealer Name` " +
                    "ORDER BY SalesCount DESC;", resultArea, new ResultFormatter() {
                @Override
                public void formatResult(ResultSet resultSet) throws SQLException {
                    while (resultSet.next()) {
                        String DealerName = resultSet.getString("Dealer Name");
                        int SalesCount = resultSet.getInt("SalesCount");

                        resultArea.append("經銷商名稱： " + DealerName + "\n");
                        resultArea.append("銷售數量： : " + SalesCount + "\n");
                        resultArea.append("-----------------------------------\n");
                    }
                }
            });
        }
    });}


    

    private static void executeQuery(String query, JTextArea resultArea, ResultFormatter formatter) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            formatter.formatResult(resultSet);

        } catch (SQLException e) {
            resultArea.setText("Query execution failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    interface ResultFormatter {
        void formatResult(ResultSet resultSet) throws SQLException;
    }
}
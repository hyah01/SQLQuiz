package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;

public class Main {
    static int score;
    static int point;
    static String name;
    static String quizDifficulty;
    static String quizAnswer;
    static BufferedReader getInput = new BufferedReader( new InputStreamReader(System.in));
    public static void main(String[] args) throws Exception{
        String url = "jdbc:mysql://localhost:3306/sql_quiz";
        String username = "root";
        String password = "gensparksql";
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection(url,username,password);
        Statement statement = connection.createStatement();

        String query = "SELECT * FROM sqlquiz";
        ResultSet result = null;
        System.out.println("Welcome to SQL quiz");
        System.out.println("Please Enter Your Name:");
        name = getInput.readLine();
        System.out.println("""
        Select Your Difficulty:
        1) Easy
        2) Medium
        3) Hard
        """);
        while(true){
            try{
                point = Integer.parseInt(getInput.readLine());
                quizDifficulty = switch(point){
                    case 2 -> "medium";
                    case 3 -> "hard";
                    default -> "easy";
                };
                break;
            } catch (NumberFormatException e){
                System.out.println("Try 1 to 3");
            }
        }
        try {
            result = statement.executeQuery(query);
            while (result.next()){
                String question = result.getString(2);
                String answer = result.getString(3);
                String difficulty = result.getString(4);
                if (difficulty.equals(quizDifficulty)){
                    System.out.println(question);
                    quizAnswer = getInput.readLine();
                    if (quizAnswer.replaceAll("\\s","").equalsIgnoreCase(answer.replaceAll("\\s",""))){
                        score += point;
                        System.out.println("Correct!");
                    } else {
                        System.out.println("The correct answer is:");
                        System.out.println(answer);
                    }

                }
            }
            System.out.println("\nYour Score is: " + score);
        } catch (SQLException e){
            e.printStackTrace();
        }

        String readQuery = "SELECT * FROM highscore WHERE name = '" + name + "'";
        try {
            result = statement.executeQuery(readQuery);
            if (result.next()){
                if (result.getInt("score") < score){
                    String updateQuery = "UPDATE highscore SET score = ? WHERE name = ?";
                    PreparedStatement createStatement = connection.prepareStatement(updateQuery);
                    createStatement.setString(2,name);
                    createStatement.setInt(1,score);
                    createStatement.executeUpdate();
                    System.out.println(" Your Score has been successfully updated");
                }
            } else{
                String createQuery = "INSERT INTO highscore (name, score) VALUES (? , ?)";
                PreparedStatement createStatement = connection.prepareStatement(createQuery);
                createStatement.setString(1,name);
                createStatement.setInt(2,score);
                createStatement.executeUpdate();
                System.out.println(" Your Score has been successfully added");
            }
            readQuery = "SELECT name,score FROM highscore LIMIT 10";
            result = statement.executeQuery(readQuery);
            System.out.println("\n--== Leader Board ==--");
            while(result.next()){
                System.out.println(result.getString("name") + " : " + result.getInt("score"));
            }

        } catch (SQLException e){
            e.printStackTrace();

        }



    }
}
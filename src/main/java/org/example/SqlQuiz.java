package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;

public class SqlQuiz {
    // Initialize variables
    static int score;
    static int point;
    static String name;
    static String quizDifficulty;
    static String quizAnswer;
    static String url;
    static String username;
    static String password;
    static Connection connection;
    static Statement statement;
    static String query;
    static ResultSet result;

    // Input stream
    static BufferedReader getInput = new BufferedReader( new InputStreamReader(System.in));

    static void Connect() throws ClassNotFoundException, SQLException {
        // Set up connect to local database
        url = "jdbc:mysql://localhost:3306/sql_quiz";
        username = "root";
        password = "gensparksql";
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection(url,username,password);
        statement = connection.createStatement();

        // Query to get all the questions, answers, difficulties
        query = "SELECT * FROM sqlquiz";
        result = null;
    }
    static void SelectDifficulty() throws IOException {
        // Beginning of quiz
        System.out.println("Welcome to SQL quiz");
        System.out.println("Please Enter Your Name:");
        name = getInput.readLine();
        // Based on what user put will give them that number of scored based on the difficulty
        System.out.println("""
        Select Your Difficulty:
        1) Easy
        2) Medium
        3) Hard
        """);
        while(true){
            try{
                // Only allow user to pick difficulty 1-3 anything else will ask again
                point = Integer.parseInt(getInput.readLine());
                quizDifficulty = switch(point){
                    case 1 -> "easy";
                    case 2 -> "medium";
                    case 3 -> "hard";
                    default -> throw new NumberFormatException();
                };
                break;
            } catch (NumberFormatException e){
                System.out.println("Try 1 to 3");
            }
        }
    }

    static void quiz(){
        try {
            // Set up query to retrieve the questions and answers
            result = statement.executeQuery(query);
            while (result.next()){
                String question = result.getString(2);
                String answer = result.getString(3);
                String difficulty = result.getString(4);
                // Only print out question that correlate to the difficulty the user picked
                if (difficulty.equals(quizDifficulty)){
                    System.out.println("Question:" + question);
                    quizAnswer = getInput.readLine();
                    // Answer will ignore spaces and capitalization for user's convenient
                    if (quizAnswer.replaceAll("\\s","").equalsIgnoreCase(answer.replaceAll("\\s",""))){
                        // If answered correctly, points will be rewarded
                        score += point;
                        System.out.println("Correct!");
                    } else {
                        // If user answered question wrong, it will tell them the answers
                        System.out.println("The correct answer is:");
                        System.out.println(answer+"\n");
                    }

                }
            }
            // Print out score at the end
            System.out.println("\nYour Score is: " + score);
        } catch (SQLException e){
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void UpdateScore(){
        // Check if data back can retrieve the name from the database
        String readQuery = "SELECT * FROM highscore WHERE name = '" + name + "'";
        try {
            // Check if user is already in the database
            result = statement.executeQuery(readQuery);
            if (result.next()){
                // If user is in database and current score is better than the one stored, update them
                if (result.getInt("score") < score){
                    String updateQuery = "UPDATE highscore SET score = ? WHERE name = ?";
                    PreparedStatement createStatement = connection.prepareStatement(updateQuery);
                    createStatement.setString(2,name);
                    createStatement.setInt(1,score);
                    createStatement.executeUpdate();
                    System.out.println(" Your Score has been successfully updated");
                }
            } else{
                // If user isn't in database, add them to the database with their score
                String createQuery = "INSERT INTO highscore (name, score) VALUES (? , ?)";
                PreparedStatement createStatement = connection.prepareStatement(createQuery);
                createStatement.setString(1,name);
                createStatement.setInt(2,score);
                createStatement.executeUpdate();
                System.out.println(" Your Score has been successfully added");
            }
            // Only print out top 10 scorer
            readQuery = "SELECT name,score FROM highscore ORDER BY score DESC LIMIT 10";
            result = statement.executeQuery(readQuery);
            // Print out leader board
            System.out.println("\n--== Leader Board ==--");
            while(result.next()){
                System.out.println(result.getString("name") + " : " + result.getInt("score"));
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws Exception{
        Connect();
        SelectDifficulty();
        quiz();
        UpdateScore();
    }
}
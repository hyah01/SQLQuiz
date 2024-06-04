import com.mysql.cj.protocol.a.ByteArrayValueEncoder;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;

import static org.junit.Assert.*;

public class QuizTest {

    // Test to see if name already exist in highscore database
    @Test
    public void testIfUserExist() throws ClassNotFoundException, SQLException {
        String url = "jdbc:mysql://localhost:3306/sql_quiz";
        String username = "root";
        String password = "gensparksql";
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection(url,username,password);
        Statement statement = connection.createStatement();
        // Hy is already in the database, so it should return true
        String readQuery = "SELECT * FROM highscore WHERE name = 'Hy'";
        ResultSet result = statement.executeQuery(readQuery);
        assertTrue(result.next());
        // Bye is not in the database, so it should return false;
        readQuery = "SELECT * FROM highscore WHERE name = 'Bye'";
        result = statement.executeQuery(readQuery);
        assertFalse(result.next());
    }

    @Test
    public void testDifficltySelection(){
        // Simulate user input of 1 4 and g
        String[] simulatedInput = {"1\n","4\n","g\n"};
        String[] result = new String[3];
        for (int i = 0; i < 3; i++){
            ByteArrayInputStream inputStream = new ByteArrayInputStream(simulatedInput[i].getBytes());
            BufferedReader getInput = new BufferedReader( new InputStreamReader(inputStream));
            try{
                // Only allow user to pick difficulty 1-3 anything else will ask again
                int point = Integer.parseInt(getInput.readLine());
                result[i] = switch(point){
                    case 1 -> "easy";
                    case 2 -> "medium";
                    case 3 -> "hard";
                    default -> throw new NumberFormatException();
                };
            } catch (NumberFormatException | IOException e){
                result[i] = "Try 1 to 3";
            }
        }
        // Any input that is from 1-3 will return it corresponding result
        assertEquals("easy",result[0]);
        // Any other input will return try 1 to 3
        assertEquals("Try 1 to 3",result[1]);
        assertEquals("Try 1 to 3",result[2]);

    }

    @Test
    public void testQuestionAnswer() throws ClassNotFoundException, SQLException {
        String[] simulatedInput = {"SELECT name FROM employees;\n","SELECT * FROM customers;\n","SELECT * FROM customers;\n","g\n","g\n"};
        int[] results = {1,2,2,2};

        String url = "jdbc:mysql://localhost:3306/sql_quiz";
        String username = "root";
        String password = "gensparksql";
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection(url,username,password);
        Statement statement = connection.createStatement();
        String query = "SELECT * FROM sqlquiz";
        ResultSet result = statement.executeQuery(query);

        int score = 0;
        int point = 1;
        int i = 0;
        try {
            // Set up query to retrieve the questions and answers
            while (result.next()) {
                String question = result.getString(2);
                String answer = result.getString(3);
                String difficulty = result.getString(4);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(simulatedInput[i].getBytes());
                System.setIn(inputStream);
                BufferedReader getInput = new BufferedReader(new InputStreamReader(System.in));
                // Only print out question that correlate to the difficulty the user picked
                if (difficulty.equals("easy")) {
                    System.out.println("Question:" + question);
                    String quizAnswer = getInput.readLine();
                    // Answer will ignore spaces and capitalization for user's convenient
                    if (quizAnswer.replaceAll("\\s", "").equalsIgnoreCase(answer.replaceAll("\\s", ""))) {
                        // If answered correctly, points will be rewarded
                        score += point;
                        System.out.println("Correct!");
                        assertEquals(results[i],score);
                    } else {
                        // If user answered question wrong, it will tell them the answers
                        System.out.println("The correct answer is:");
                        System.out.println(answer + "\n");
                        assertEquals(results[i],score);
                    }
                    i++;
                }
            }
            // Print out score at the end
            System.out.println("\nYour Score is: " + score);
            assertEquals(2,score);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}

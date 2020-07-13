import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.lang.System.exit;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import tuluBox.DiscordWebhook;

public class SelScraper {

    public static final String FIN_DIR = "tuluBox/finisher.txt";
    public static final String START_DIR = "tuluBox/starter.txt";
    public static final String DELETE_TMP_DIR = "tuluBox/del tmp.cmd";
    public static final String C_DRIVER_EXE_DIR = "tuluBox/chromedriver.exe";

    PrintStream std_out = System.out;
    StringBuilder bal_from_file = new StringBuilder();
    StringBuilder stale_bets = new StringBuilder();
    Stream<String> stale_bets_streamer;
    WebDriver driver;
    ChromeOptions options = new ChromeOptions();
    String link_id, selected_match_name;
    int game_bet_counter = 0;
    int num_live;
    boolean doggo_at_rest = true;

    JavascriptExecutor jsExecutor;
    String currentJsQuery;
    //Use this to store element where we will have multiple consecutive interactions with it.
    WebElement pendingElement;

    String sauce_div;
    String[] already_bet;
    double initial_bal, ex_bal, now_bal;
    boolean first_login = true;


    public void setOptions() {
        System.setProperty("webdriver.chrome.driver", C_DRIVER_EXE_DIR);
        options.addArguments("--headless");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-gpu");
        driver = new ChromeDriver(options);

        jsExecutor = (JavascriptExecutor) driver;
    }


    private void login() {
        setOptions();
        try {
            driver.get("https://odibets.com");
            driver.findElement(By.id("mobile-web-login")).click();
            driver.findElement(By.xpath("//input[@type='tel']")).sendKeys("<<USERNAME>>");
            driver.findElement(By.xpath("//input[@type='password']")).sendKeys("<<PASSWORD>>");
            driver.findElement(By.xpath("//button[@type='submit']")).click();
        } catch (WebDriverException e) {
            e.printStackTrace();
            System.out.println("Login failed, check the internet connection");

            try {
                TimeUnit.SECONDS.sleep(30);
            } catch (InterruptedException in) {
                System.out.println("Sleep Interrupted");
            }
            try {

                Runtime.getRuntime().exec(DELETE_TMP_DIR);
            } catch (IOException V) {
                System.out.println("Command not executed");
            }
        }
    }


    private String readFromFile(String file_location) {
        try {
            FileReader fr = new FileReader(file_location);
            int i;
            while ((i = fr.read()) != -1)
                bal_from_file.append((char) i);
        } catch (FileNotFoundException f) {
            f.printStackTrace();
        } catch (IOException f) {
            f.printStackTrace();
        }
        return bal_from_file.toString();
    }

    private void writeToFile(String file_location, String message) {
        try {
            PrintStream fileOut = new PrintStream(file_location);
            System.setOut(fileOut);
            System.out.println(message);
            System.setOut(std_out);
        } catch (IOException f) {
            f.printStackTrace();
        }
    }

    private String staleBets(String[] already_bet) {
        stale_bets.setLength(0);
        stale_bets_streamer = Stream.of(already_bet);
        stale_bets_streamer.forEach(stale_bet_id -> stale_bets.append("and not(a[contains(@href,'" + stale_bet_id + "')])"));

        return stale_bets.toString();
    }

    private String betOnGudOddz() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException in) {
            System.out.println("Sleep Interrupted");
        }

        try {
            if (first_login == true) {
                currentJsQuery = "return document.evaluate(\"//a[@class='mybal']/span/text()\", document, null, XPathResult.STRING_TYPE, null).stringValue";
                try {
                    try {
                        initial_bal = Double.parseDouble(readFromFile(START_DIR));
                        System.out.println("Continuing from interrupted Session of Initial Investment: " + initial_bal);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        initial_bal = Double.parseDouble(jsExecutor.executeScript(currentJsQuery).toString().replace("/-", ""));
                        writeToFile(START_DIR, String.valueOf(initial_bal));
                        System.out.println("Starting fresh session with Investment: " + initial_bal);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    System.out.println("Failed to load balance, retrying");
                    login();
                }
                first_login = false;
            }
            try {
                //driver.get("https://odibets.com/mybets");

                currentJsQuery = "return document.evaluate(\"//a[@class='mybal']/span/text()\", document, null, XPathResult.STRING_TYPE, null).stringValue";


                try {
                    now_bal = Double.parseDouble(jsExecutor.executeScript(currentJsQuery).toString().replace("/-", ""));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    System.out.println("Failed to load balance, retrying");
                    login();
                }


            } catch (WebDriverException f) {
                f.printStackTrace();
            }
            ex_bal = now_bal - initial_bal;
            if (ex_bal >= 0.7) {

                try {
                    DiscordWebhook webhook = new DiscordWebhook("https://discord.com/api/webhooks/728967323515486289/F459Me_olUD2KdJqYP8iP92el8vd_9IKHzkk5_3z1dWsplK4NS2vNdxsD4ps1BDIYhWC");
                    webhook.setContent("Doggo has achieved aims for the day. Closing kennel and shop." + initial_bal + " to " + now_bal + ".");
                    webhook.setUsername("OdiDoggo");
                    webhook.execute();
                } catch (IOException g) {
                    g.printStackTrace();
                    System.out.println("Doggo Unable to send Mail");
                }

                System.out.println("Doggo has achieved aims for the day. Closing kennel and shop");
                try {

                    driver.get("https://odibets.com/mybets");
                    int pending_bets = driver.findElements(By.xpath("//div[@class='l-mybets-section-container show']")).size();
                    driver.quit();
                    writeToFile(FIN_DIR, "Closing with expk of ksh. " + (now_bal + 0.7) + "\nBalance is ksh. " + now_bal + " and " + pending_bets + " pending bets with initial investment ksh. " + initial_bal);
                    writeToFile(START_DIR, "WOOF!");
                    Runtime.getRuntime().exec(DELETE_TMP_DIR);
                } catch (IOException V) {
                    System.out.println("Kennel door not well closed tho");
                }
                exit(0);
            }


            if (now_bal <= 1) {


                try {

                    System.out.println("Account depleted, waiting for pending bets to mature");
                    TimeUnit.MINUTES.sleep(10);
                } catch (InterruptedException in) {
                    System.out.println("Sleep Interrupted");
                }


                return "Can do dis all day";
            }


            System.out.println("starting access");


            driver.get("https://odibets.com/live");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                if (doggo_at_rest == true) {
                    num_live = driver.findElements(By.xpath("//div[@class='l-games-event' and not(div[@disabled='disabled'])]/div/div/div/button[ substring(@custom, string-length(@custom) -1)='11']")).size();

                    already_bet = new String[num_live];
                    Arrays.fill(already_bet, "null");
                    System.out.println("Found " + num_live + " Live Matches.");
                    doggo_at_rest = false;
                }
            } catch (NumberFormatException n) {
                System.out.println("Failed to find number of soccer matches, retrying.");
                return "Can do dis all day";
            }

        } catch (WebDriverException g) {
            //g.printStackTrace();
            //login();

            System.out.println("Logged out. Doggo logging back in");
            return "Can do dis all day";
        }
        try {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException in) {
                System.out.println("Sleep Interrupted");
            }

            sauce_div = "//div[@class='l-games-event' and not(div[@disabled='disabled'])"/*number(div/div[@class='event-scores-1']/span[contains(@id,'h_')])+number(div/div[@class='event-scores-1']/span[contains(@id,'a_')])>-1  and */
                    + staleBets(already_bet) + "]/div/div/div";///span[contains(@id,'a_')]>1]/div[@class='event-market']/div/button")).click();//and @oddvalue>1.0 and  span/img/@src='https://s3-eu-west-1.amazonaws.com/odibets/img/i-down.png']")).click();


            selected_match_name = driver.findElement(By.xpath("//div[@class='l-games-event' and not(div[@disabled='disabled'])" + staleBets(already_bet) + "]/a/div/div[@class='t-i']")).getText();
            link_id = driver.findElement(By.xpath("//div[@class='l-games-event' and not(div[@disabled='disabled'])" + staleBets(already_bet) + "]/a")).getAttribute("href").replace("/match-details?id=", "").replace("https://odibets.com", "");

            //Show current game in consideration
            System.out.println(selected_match_name);


            already_bet[game_bet_counter] = link_id;
            game_bet_counter++;
            //f.printStackTrace();

            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //previous exp range(1.18-1.30)
            currentJsQuery = "document.evaluate(\"//div[@class='l-games-event' and a[@href='/match-details?id=" + link_id + "']]/div/div/div/button[span[text()>1.18 and text()<1.39] and( substring(@custom, string-length(@custom) -1)='11')]\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.click();";
            jsExecutor.executeScript(currentJsQuery);
        } catch (NoSuchElementException | ElementClickInterceptedException | JavascriptException g) {//("//button[@oddvalue<'1.5' and @oddvalue>'1.2']")).click();}catch (NoSuchElementException |ElementClickInterceptedException g){
            //g.printStackTrace();

            System.out.println("odds less than 1.4 not found or click intercepted.");
            checkIfBreakTime();

            return "Can do dis all day";
        }

        System.out.println("found bet");
        driver.findElement(By.id("betslip-bottom-betslip")).click();

        pendingElement = driver.findElement(By.xpath("//input[@type='number']"));
        try {
            if (!pendingElement.getText().equals("1")) {
                pendingElement.clear();


                pendingElement.sendKeys("1");

                try {//this event implies the games are too few
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException in) {
                    System.out.println("Sleep Interrupted");
                }

                driver.findElement(By.xpath("//div[@class='ct']/button")).click();


                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

//                //Remove pending bets to start with clean sheet
//                driver.findElement(By.id("betslip-bottom-betslip")).click();
//                driver.findElement(By.xpath("//div[@class='top-remove']")).click();

            }
        } catch (NoSuchElementException | ElementClickInterceptedException f) {
            System.out.println("Bet placed one touch");
        }
            /*
            File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            // Now you can do whatever you need to do with it, for example copy somewhere
            try {
                FileUtils.copyFile(scrFile, new File("A:\\Engineering order\\Project Bet\\bets\\bet"+betNo+".jpg"));
            } catch (IOException a) {
                System.out.println("Screenshot save failure");
            }*/

        System.out.println("bet placed");
        checkIfBreakTime();


        return "Can do dis all day";
    }


    private void checkIfBreakTime() {
        try {
            if (game_bet_counter == num_live) {
                doggo_at_rest = true;
                System.out.println("Doggo has bet too much, taking fresh air");
                Runtime.getRuntime().exec(DELETE_TMP_DIR);
                TimeUnit.MINUTES.sleep(4);
                game_bet_counter = 0;
            }
        } catch (InterruptedException in) {
            System.out.println("Sleep Interrupted");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }


    public static void main(String[] args) {
        SelScraper odidoggo = new SelScraper();
        odidoggo.setOptions();
        odidoggo.driver.quit();
        odidoggo.login();
        while (true) {


            if (odidoggo.betOnGudOddz().equals("Can do dis all day")) {
                System.out.println("Onto the next.");
            } else {
                break;
            }
        }

        exit(0);
    }
}

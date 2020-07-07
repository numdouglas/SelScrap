import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.lang.System.exit;

import org.w3c.dom.Document;
import tuluBox.DiscordWebhook;

public class SelScraper {

    public static final String FIN_DIR = "tuluBox/finisher.txt";
    public static final String START_DIR = "tuluBox/starter.txt";
    public static final String DELETE_TMP_DIR = "tuluBox/del tmp.cmd";
    public static final String C_DRIVER_EXE_DIR = "tuluBox/chromedriver.exe";

    PrintStream std_out = System.out;
    StringBuilder bal_from_file=new StringBuilder();
    StringBuilder stale_bets=new StringBuilder();
    Stream<String> stale_bets_streamer;
    WebDriver driver;
    ChromeOptions options = new ChromeOptions();
    String link_id;
    int game_bet_counter=0;

    JavascriptExecutor jsExecutor;
    String currentJsQuery;
    //Use this to store element where we will have multiple consecutive interactions with it.
    WebElement pendingElement;

    String sauce_div;
    String[] already_bet;
    double initial_bal, pending_bets,ex_bal,now_bal;
    boolean first_login=true;



    public void setOptions() {
        System.setProperty("webdriver.chrome.driver", C_DRIVER_EXE_DIR);
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-extensions");
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        driver = new ChromeDriver(options);

        jsExecutor=(JavascriptExecutor)driver;
    }



    private void login() {
        setOptions();
        try{
            driver.get ("https://odibets.com");
            driver.findElement(By.id("mobile-web-login")).click();
            driver.findElement(By.xpath("//input[@type='tel']")).sendKeys("0741287087");
            driver.findElement(By.xpath("//input[@type='password']")).sendKeys("dagi90210");
            driver.findElement(By.xpath("//button[@type='submit']")).click();
        }catch (WebDriverException e){
            e.printStackTrace();
            System.out.println("Login failed, check the internet connection");

            try{
                TimeUnit.SECONDS.sleep(30);}
            catch (InterruptedException in){System.out.println("Sleep Interrupted"); }
            try {

                Runtime.getRuntime().exec(DELETE_TMP_DIR);}
            catch (IOException V){System.out.println("Command not executed");
            }
        }
    }


    private String readFromFile(String file_location){
        try{
            FileReader fr = new FileReader(file_location);
            int i;
            while ((i=fr.read()) != -1)
                bal_from_file.append((char)i);}

        catch (FileNotFoundException f){f.printStackTrace();}
        catch (IOException f){f.printStackTrace();}
        return bal_from_file.toString();}

    private void writeToFile(String file_location,String message){
        try{
            PrintStream fileOut = new PrintStream(file_location);
            System.setOut(fileOut);
            System.out.println(message);
            System.setOut(std_out);}
        catch (IOException f){f.printStackTrace();}
    }

    private String staleBets(String[] already_bet){
        stale_bets_streamer=Stream.of(already_bet);
        stale_bets_streamer.forEach(stale_bet->stale_bets.append(" and not(a/div/div[contains(text(),'"+stale_bet+"')])"));

        return stale_bets.toString();
    }

    private String betOnGudOddz(){
        int times_trashed=0;
        try{
            TimeUnit.SECONDS.sleep(3);}
        catch (InterruptedException in){System.out.println("Sleep Interrupted"); }

        do{
            try {
                if(first_login==true){
                    currentJsQuery="return document.evaluate(\"//a[@class='mybal']/span/text()\", document, null, XPathResult.STRING_TYPE, null).stringValue";
                    try{
                        try{
                        initial_bal=Double.parseDouble(readFromFile(START_DIR));
                        System.out.println("Continuing from interrupted Session of Initial Investment: "+initial_bal);}
                        catch (NumberFormatException e){
                            e.printStackTrace();
                        initial_bal=Double.parseDouble(jsExecutor.executeScript(currentJsQuery).toString().replace("/-",""));
						writeToFile(START_DIR,String.valueOf(initial_bal));
                        System.out.println("Starting fresh session with Investment: "+initial_bal);}
                    }
                    catch (NumberFormatException e){e.printStackTrace();
                        System.out.println("Failed to load balance, retrying");
                        return "Can do dis all day";
                    }
                }
                try{
                    driver.get("https://odibets.com/mybets");

                    currentJsQuery="return document.evaluate(\"//a[@class='mybal']/span/text()\", document, null, XPathResult.STRING_TYPE, null).stringValue";


                    try{now_bal=Double.parseDouble(jsExecutor.executeScript(currentJsQuery).toString().replace("/-",""));}
                    catch (NumberFormatException e){
                        e.printStackTrace();
                        System.out.println("Failed to load balance, retrying");
                        return "Can do dis all day";
                    }

                    pending_bets=driver.findElements(By.xpath("//div[@class='l-mybets-section-container show']")).size();
                }
                catch (WebDriverException f){
                    pending_bets=0;}
                ex_bal=(now_bal+(pending_bets/2))-initial_bal;
                if(ex_bal>=2){

                    try{
                    DiscordWebhook webhook=new DiscordWebhook("https://discord.com/api/webhooks/728967323515486289/F459Me_olUD2KdJqYP8iP92el8vd_9IKHzkk5_3z1dWsplK4NS2vNdxsD4ps1BDIYhWC");
                    webhook.setContent("Doggo has achieved aims for the day. Closing kennel and shop."+initial_bal+" to "+now_bal+pending_bets+".");
                    webhook.setUsername("OdiDoggo");
                    webhook.execute();}
                    catch (IOException g){
                        g.printStackTrace();
                        System.out.println("Doggo Unable to send Mail");
                    }

                    System.out.println("Doggo has achieved aims for the day. Closing kennel and shop");
                    try {
                        driver.quit();
                        writeToFile(FIN_DIR,"Closing with expk of ksh. "+(now_bal+pending_bets)+"\nBalance is ksh. "+now_bal+ " and "+pending_bets+" pending bets with initial investment ksh. "+initial_bal);
                        writeToFile(START_DIR,"WOOF!");
                        Runtime.getRuntime().exec(DELETE_TMP_DIR);
                    }
                    catch (IOException V){
                        System.out.println("Kennel door not well closed tho");
                    }
                    exit(0);
                }



                if (now_bal <= 1) {


                    try {

                        System.out.println("Account depleted, waiting for pending bets to mature");
                        TimeUnit.MINUTES.sleep(10);
                        times_trashed++;
                    } catch (InterruptedException in) {
                        System.out.println("Sleep Interrupted");
                    }


                    return "Can do dis all day";
                }


                System.out.println("starting access");
                driver.get("https://odibets.com/live");
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentJsQuery="document.evaluate(\"//div[span[text()='Soccer']]\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.click();";
                jsExecutor.executeScript(currentJsQuery);
                currentJsQuery="return document.evaluate(\"//div[span[text()='Soccer']]/span[2]\", document, null, XPathResult.STRING_TYPE, null).stringValue;";
                try {
                    if(first_login==true){
                        int num_matches=Integer.parseInt(jsExecutor.executeScript(currentJsQuery).toString());

                    already_bet = new String[num_matches];
                    Arrays.fill(already_bet,"null");
                    System.out.println("Found "+num_matches+" Live Matches.");
                    first_login=false;}
                }catch (NumberFormatException n){
                    System.out.println("Failed to find number of soccer matches, retrying.");
                    return "Can do dis all day";
                }

            }catch(WebDriverException g){
                g.printStackTrace();
                //login();

                System.out.println("Logged out. Doggo logging back in");return "Can do dis all day";}
            try{
                try{
                    TimeUnit.SECONDS.sleep(3);}
                catch (InterruptedException in){System.out.println("Sleep Interrupted"); }

                try{sauce_div= "//div[@class='l-games-event' and not(div[@disabled='disabled'])"/*number(div/div[@class='event-scores-1']/span[contains(@id,'h_')])+number(div/div[@class='event-scores-1']/span[contains(@id,'a_')])>-1  and */
                        +staleBets(already_bet)+"]/div/div/div";///span[contains(@id,'a_')]>1]/div[@class='event-market']/div/button")).click();//and @oddvalue>1.0 and  span/img/@src='https://s3-eu-west-1.amazonaws.com/odibets/img/i-down.png']")).click();

                }catch (NullPointerException v){v.printStackTrace();

                     }
                link_id= driver.findElement(By.xpath(sauce_div.replace("/div/div/div","/a"))).getText().substring(0,7);

                //Show current game in consideration
                System.out.println(link_id);

                try {
                    already_bet[game_bet_counter]=link_id;
                    game_bet_counter++;
                }
                catch (ArrayIndexOutOfBoundsException f){
                    //f.printStackTrace();
                    try {
                        System.out.println("Doggo has bet too much, taking fresh air");
                        Runtime.getRuntime().exec(DELETE_TMP_DIR);
                        TimeUnit.MINUTES.sleep(4);
                        game_bet_counter=0;
                        Arrays.fill(already_bet, "null");}
                    catch (InterruptedException in){System.out.println("Sleep Interrupted"); }
                    catch (IOException i){i.printStackTrace();}}



                try {
                    TimeUnit.SECONDS.sleep(4);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentJsQuery ="document.evaluate(\""+sauce_div+"/button[span[text()>1.18 and text()<1.30] and( substring(@custom, string-length(@custom) -1)='11')]\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.click();";
                jsExecutor.executeScript(currentJsQuery);
                //driver.findElement(By.xpath("//button[contains(text(),'1.']")).click();// | //button[not(@disabled) and span[text()>'1.1'] and span[text()<'1.24'] and substring(@custom, string-length(@custom) -1)='500']")).click();
            }catch (NoSuchElementException |ElementClickInterceptedException | JavascriptException g){//("//button[@oddvalue<'1.5' and @oddvalue>'1.2']")).click();}catch (NoSuchElementException |ElementClickInterceptedException g){
                //g.printStackTrace();
                try {//this event implies the games are too few

                    System.out.println("odds less than 1.4 not found or click intercepted, trying again....");
                    TimeUnit.SECONDS.sleep(4);
                    //game_bet_counter=0;

                    game_bet_counter--;
                    already_bet[game_bet_counter]="filled";
                    //System.out.println("Correction, Game "+game_bet_counter+" ="+already_bet[game_bet_counter]);
                    game_bet_counter++;
                    //Arrays.fill(already_bet, "null");
                    times_trashed++;}
                catch (InterruptedException in){System.out.println("Sleep Interrupted"); }

                return "Can do dis all day"; }

            System.out.println("found bet");
            driver.findElement(By.id("betslip-bottom-betslip")).click();

            pendingElement=driver.findElement(By.xpath("//input[@type='number']"));
            try{if(!pendingElement.getText().equals("1")) {
                pendingElement.clear();


                pendingElement.sendKeys("1");

//                driver.findElement(By.name("stake")).sendKeys("0");
                try {//this event implies the games are too few


                    TimeUnit.SECONDS.sleep(2);
                    //game_bet_counter=0;
                    //Arrays.fill(already_bet, "null");
                    times_trashed++;}
                catch (InterruptedException in){System.out.println("Sleep Interrupted"); }

               driver.findElement(By.xpath("//div[@class='ct']/button")).click();


                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                driver.get("https://odibets.com");
                //Remove pending bets to start with clean sheet
                driver.findElement(By.id("betslip-bottom-betslip")).click();
                driver.findElement(By.xpath("//div[@class='top-remove']")).click();

                }}catch (NoSuchElementException|ElementClickInterceptedException f){System.out.println("Bet placed one touch");}
            /*
            File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            // Now you can do whatever you need to do with it, for example copy somewhere
            try {
                FileUtils.copyFile(scrFile, new File("A:\\Engineering order\\Project Bet\\bets\\bet"+betNo+".jpg"));
            } catch (IOException a) {
                System.out.println("Screenshot save failure");
            }*/

            System.out.println("bet placed");


            //driver.findElement(By.xpath("//img[@src='https://s3-eu-west-1.amazonaws.com/odibets/img/menu/odi-live.png']")).click();
            driver.get("https://odibets.com/live?sport=1");

            try {


                TimeUnit.SECONDS.sleep(22);}
            catch (InterruptedException G){System.out.println("Sleep Interrupted");}
            times_trashed++;}


        while (times_trashed<9);

        return "Can do dis all day";    }



    public  static void main(String[] args) {
        SelScraper odidoggo = new SelScraper();
        odidoggo.setOptions();
        odidoggo.driver.quit();
        odidoggo.login();
        while (true){


            if (odidoggo.betOnGudOddz().equals("Can do dis all day")){
                System.out.println("Onto the next."); }
            else {
                break;
            }}

        exit(0);}}
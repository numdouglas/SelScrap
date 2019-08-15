import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public class SelScraper {

    public static final String FIN_DIR = "tuluBox/finisher.txt";
    public static final String DELETE_TMP_DIR = "tuluBox/del tmp.cmd";
    public static final String C_DRIVER_EXE_DIR = "tuluBox/chromedriver.exe";
    WebDriver driver;
    ChromeOptions options = new ChromeOptions();
    String link_id;
    int game_bet_counter=0;

    WebElement sauce_div;
    String[] already_bet=new String[10];
    double initial_bal, pending_bets,ex_bal,now_bal;
    boolean first_login=true;

    public void setOptions() {
        System.setProperty("webdriver.chrome.driver", C_DRIVER_EXE_DIR);
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-extensions");
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        driver = new ChromeDriver(options);
    }


    private void login() {
        setOptions();
        try{

        driver.get ("https://odibets.com/login");
        driver.findElement(By.name("msisdn")).sendKeys("0741287087");
        driver.findElement(By.name("password")).sendKeys("dagi90210");
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        }catch (WebDriverException e){
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



    private String betOnGudOddz(){
        int times_trashed=0;

        do{
            try {
                if(first_login==true){

                initial_bal=   Double.parseDouble(driver.findElement(By.xpath("//a[@class=\"l-account-links active\"]")).getText().replace("Account: ", "").replace(",",""));
                first_login=false;}
                try{
                    now_bal=Double.parseDouble(driver.findElement(By.xpath("//a[@class=\"l-account-links active\"]")).getText().replace("Account: ", "").replace(",",""));
                pending_bets=Double.parseDouble(driver.findElement(By.xpath("//a[@href=\"/my-bets\"]")).getText().replace("My Bets","").replace(" [","").replace("]",""));}
                catch (WebDriverException f){
                    pending_bets=0;}
                ex_bal=(now_bal+(pending_bets/2))-initial_bal;
                if(ex_bal>=2){

                System.out.println("Doggo has achieved aims for the day. Closing kennel and shop");
                try {
                    driver.quit();
                    PrintStream fileOut = new PrintStream(FIN_DIR);
                    System.setOut(fileOut);
                    System.out.println("Closing with expk of ksh. "+(now_bal+pending_bets)+"\nBalance is ksh. "+now_bal+ " and "+pending_bets+" pending bets with initial investment ksh. "+initial_bal);
                    Runtime.getRuntime().exec(DELETE_TMP_DIR);
                }
                catch (IOException V){
                    System.out.println("Kennel door not well closed tho");
                }
                System.exit(0);
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
            driver.get("https://odibets.com/live?sport=1");
            }catch(WebDriverException g){
                login();

                System.out.println("Logged out. Doggo logging back in");return "Can do dis all day";}
            try{

                try{sauce_div= driver.findElement(By.xpath("//div[@class='event' and not(div[@class='event-locked']) and "/*number(div/div[@class='event-scores-1']/span[contains(@id,'h_')])+number(div/div[@class='event-scores-1']/span[contains(@id,'a_')])>-1  and */+"not(a/span[contains(text(),'"+already_bet[0]+"')]) and not(a/span[contains(text(),'"+already_bet[1]+"')])" +
                        " and not(a/span[contains(text(),'"+already_bet[2]+"')]) and not(a/span[contains(text(),'"+already_bet[3]+"')])" +
                        " and not(a/span[contains(text(),'"+already_bet[4]+"')]) and not(a/span[contains(text(),'"+already_bet[5]+"')])" +
                        " and not(a/span[contains(text(),'"+already_bet[6]+"')]) and not(a/span[contains(text(),'"+already_bet[7]+"')])" +
                        " and not(a/span[contains(text(),'"+already_bet[8]+"')]) and not(a/span[contains(text(),'"+already_bet[9]+"')])]/a "


                ));///span[contains(@id,'a_')]>1]/div[@class='event-market']/div/button")).click();//and @oddvalue>1.0 and  span/img/@src='https://s3-eu-west-1.amazonaws.com/odibets/img/i-down.png']")).click();

                }catch (NullPointerException v){v.printStackTrace();}
                link_id=sauce_div.getText().substring(0,5);
                sauce_div.click();
                System.out.println(link_id);

                try {
                already_bet[game_bet_counter]=link_id;
                game_bet_counter++;}
                catch (ArrayIndexOutOfBoundsException f){

                        try {
                            System.out.println("Doggo has bet too much, taking fresh air");
                            TimeUnit.MINUTES.sleep(4);
                            game_bet_counter=0;
                            Arrays.fill(already_bet, "null");}
                        catch (InterruptedException in){System.out.println("Sleep Interrupted"); }}


                System.out.println(already_bet[0]);

                driver.findElement(By.xpath("//button[not(@disabled) and @outcomeid='12' and @oddvalue>1.1 and @oddvalue<1.24] | //button[not(@disabled) and @oddvalue>1.1 and @oddvalue<1.24 and @outcomeid='13']")).click();
            }catch (NoSuchElementException |ElementClickInterceptedException g){//("//button[@oddvalue<'1.5' and @oddvalue>'1.2']")).click();}catch (NoSuchElementException |ElementClickInterceptedException g){

                try {//this event implies the games are too few

                    System.out.println("odds less than 1.4 not found or click intercepted, trying again....");
                    TimeUnit.SECONDS.sleep(18);
                    //game_bet_counter=0;
                    if(game_bet_counter<9){
                    already_bet[game_bet_counter]="filled";
                    game_bet_counter++;}
                    //Arrays.fill(already_bet, "null");
                    times_trashed++;}
                catch (InterruptedException in){System.out.println("Sleep Interrupted"); }

                return "Can do dis all day"; }

            System.out.println("found bet");
            driver.findElement(By.className("slip-counter")).click();

            try{if(!driver.findElement(By.name("stake")).getText().equals("1")) {
                driver.findElement(By.name("stake")).clear();


                driver.findElement(By.name("stake")).sendKeys("1");

//                driver.findElement(By.name("stake")).sendKeys("0");
                try {//this event implies the games are too few


                    TimeUnit.SECONDS.sleep(2);
                    //game_bet_counter=0;
                    //Arrays.fill(already_bet, "null");
                    times_trashed++;}
                catch (InterruptedException in){System.out.println("Sleep Interrupted"); }
            driver.findElement(By.className("l-betslip-cta-btn")).click();
            driver.findElement(By.className("l-betslip-cta-btn")).click();}}catch (NoSuchElementException|ElementClickInterceptedException f){System.out.println("Bet placed one touch");}
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
            System.gc();
            try {


            TimeUnit.SECONDS.sleep(22);}
        catch (InterruptedException G){System.out.println("Sleep Interrupted");}
            times_trashed++;}


        while (times_trashed<9);

        return "Can do dis all day";    }



    public  static void main(String[] args){
        SelScraper odidoggo=new SelScraper();
        odidoggo.setOptions();
        odidoggo.login();
        while (true){


        if (odidoggo.betOnGudOddz().equals("Can do dis all day")){try {
            System.out.println("Cleaning up after self....");
            Runtime.getRuntime().exec(DELETE_TMP_DIR);}
            catch (IOException V){System.out.println("Command not executed");
        }
        }
            else {
                break;

            }}

        System.exit(0);}}


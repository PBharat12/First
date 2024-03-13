import java.util.Scanner;
class check_even_or_odd{
  public static void main(String args[])
  {
    Scanner sc = new Scanner(System.in);
    System.out.println("ENTER A NUMBER:");
    int x = sc.nextInt();
    if(x % 2 == 0)
    {
      System.out.println("NUMBER IS EVEN.");
    }
    else{
      System.out.println("NUMBER IS ODD.");
    }
  }  
}

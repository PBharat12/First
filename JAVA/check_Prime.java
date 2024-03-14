import java.util.Scanner;
class check_Prime{
  public static void main(String args[])
  {
    Scanner sc = new Scanner(System.in);
    System.out.println("ENTER A NUMBER:");
    int n = sc.nextInt();
    int c = 0;
    for(int i=1;i<=n;i++)
    {
      if(n%i==0)
      {
        c=c+1;
       }
    }
    if(c==2)
    {
     System.out.println("NUMBER IS PRIME.");
    }
    else
    {
     System.out.println("NUMBER IS NOT PRIME.");
     }
  }  
}

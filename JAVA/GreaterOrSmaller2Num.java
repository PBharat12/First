import java.util.Scanner;
class GreaterOrSmaller2Num{
	public static void main(String[] args){

		Scanner sc = new Scanner(System.in);
		System.out.print("Enter two Numbers:");
		int a,b;
		a=sc.nextInt();
		b=sc.nextInt();
		if(a>b){
			System.out.println("First is greater then the Second.");
		}else{
			System.out.println("Second is greater then the First");
		}
			
	}
}

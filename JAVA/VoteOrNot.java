import java.util.Scanner;
class VoteOrNot{
	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);

		System.out.print("Enter age:");

		int age = sc.nextInt();

		if(age >= 18){

			System.out.println("eligible to vote.");

		}else{

			System.out.println("not elgible to vote");
		}
	}
}
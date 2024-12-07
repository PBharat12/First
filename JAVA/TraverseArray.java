import java.util.Scanner;
class TraverseArray{
	public static void main(String[] args) {
	
		Scanner sc = new Scanner(System.in);
		System.out.print("Give size of the Array:"); //the no of elemets array can store
		int n = sc.nextInt();
		int arr[] = new int[n];
		System.out.print("Enter the Elements to the Array:");
		//input elements
		for(int i=0;i<n;i++){
			arr[i]=sc.nextInt();
		}
		//traverse
		System.out.print("Enter Array is:");
		for(int i=0;i<n;i++){
			System.out.print(" "+arr[i]);
		}
	}
}
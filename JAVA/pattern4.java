class pattern4{
	public static void main(String[] args){
		int k=1;
		for (int i=0;i<5 ;i++ ) {

			for(int j=1;j<=5;j++){

				if(k<10){
					System.out.print("0");
				}
				System.out.print(k+" ");
				k+=1;
			
			}
			System.out.println();
		}
	}
}

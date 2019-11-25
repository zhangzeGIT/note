public class tjest {

    public static void main(String[] args) {
        byte[] aa = new byte[1111111111];
        aa[1] = 1;
        for (byte b : aa) {
            System.out.println(b);
        }
    }
}

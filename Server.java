import java.awt.Color;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Collections;
import javax.swing.JFrame;
public class Server extends JFrame {
    private Socket s = null;
    private ServerSocket ss = null;
    //保存客服端的线程
    private ArrayList <client> clients=new ArrayList();
    public client c1;
    public client c2;
    public Server() throws Exception {
        this.setTitle(" 服务器 ");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBackground(Color.yellow);
        this.setSize(200, 100);
        this.setVisible(true);
        ss = new ServerSocket(9999);// 服务器开辟端口，接收连接
        try {
            for (int i = 0; i < 2; i++) {         //接受两个客户端连接，然后开始运行
                s = ss.accept();
                if(i==0) {
                     c1 = new client(s);
                     clients.add(c1);
                    c1.ps.println("请等待另一玩家加入……");
                }else
                {
                    c2=new client(s);
                    clients.add(c2);
                }
            }
            c1.start();
            c2.start();
            c2.sendMessage("start");
            Thread.sleep(2000);
            c1.sendMessage(c1.WordCreator());
        } catch (Exception ex) {
            ex.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this, " 游戏异常退出！");
            System.exit(0);
        }

    }

    class client extends Thread {//为某个Socket负责接收信息
        private Socket s = null;
        private BufferedReader br = null;
        private PrintStream ps = null;
        private boolean canRun = true;
        private String[] word = new String[4];

        public client(Socket s) throws Exception {
            this.s = s;
            br = new BufferedReader(
                    new InputStreamReader(s.getInputStream()));
            ps = new PrintStream(s.getOutputStream());
        }
        public void run() {
            try {
                while (canRun) {
                    //怎么解决超时
                     String str = br.readLine();//读该 Socket 传来的信息
                    if (str.equals("超时")&&this.equals(c1)&&clients.contains(c1)||str.equals("超时")&&!clients.contains(c1))   //时间到了没人答出来
                    {
                        sendMessage("-1");
                        str = WordCreator();
                        sendMessage(str);
                        continue;
                    }
                        /*if(str.equals("0"))
                        {
                            str = WordCreator();
                            sendMessage(str);   //发送本轮游戏的内容
                        }
                        else*/
                        if(str.equals("1")||str.equals("-1")) {
                            sendMessage(str);
                            str = this.WordCreator();
                            sendMessage(str); //将 str发给所有客服端
                        }
                }

            } catch (Exception ex) { //此处可以解决客户端异常下线的问题
                canRun = false;
                clients.remove(this);
            }
        }
    //将信息发给所有其他客户端
    public void sendMessage(String msg) {
        c1.ps.println(msg);
        c2.ps.println(msg);
    }
    public String WordCreator() throws Exception {
        String msg ;
        readword rw = new readword();
        String fw = rw.get();    //falling word
        String[] tmp = fw.split("#");
        fw = tmp[1];
        word[0] = tmp[2];   //将正确的中文先存放
        msg = fw + "#" + word[0] + "#";//储存答案
        for (int i = 1; i < 4; i++) {
            String str=rw.get();
            tmp = str.split("#");
            word[i] = tmp[2];
        }
        List list = new ArrayList<String>();
        list = Arrays.asList(word);
        Collections.shuffle(list);
        for (int i = 0; i < 4; i++) {
            msg += list.get(i) + "#";
        }
        return msg;
    }
}

    public class readword {
        File file = new File("/vocabulary.txt");
        ArrayList pos = new ArrayList();

        public readword() {
            try {
                RandomAccessFile rndfile = new RandomAccessFile(file, "r");
                String str;
                while (null != (str = rndfile.readLine())) {
                    pos.add(rndfile.getFilePointer());
                    //System.out.println(rndfile.getFilePointer());
                }
            } catch (Exception ex) {
            }
        }

        public String get() throws Exception {
            RandomAccessFile rndfile = new RandomAccessFile(file, "r");
            int position = (int) (Math.random() * pos.size());
            rndfile.seek((long) pos.get(position));
            String str = new String(rndfile.readLine().getBytes("ISO-8859-1"), "utf-8");
            return str;
        }
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();

    }
}
//服务器的功能，wordcreator 生成游戏内容，判定加分减分，解决超时
//储存词汇表每行的偏移量
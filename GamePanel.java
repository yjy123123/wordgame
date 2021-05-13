import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import javax.swing.*;
public class GamePanel extends JPanel
        implements ActionListener,KeyListener,Runnable { // 生命值
    private int life = 20;
    // 按键按下的字母
    private char keychar;
    private char temp;//move
    private int flag=0;//别人是否提前答题
    private String answer;
    //  掉下来的字母 Label
    private JLabel lbword = new JLabel();
    // 当前生命值状态显示 Label
    private JLabel lbLife = new JLabel();
    private JLabel lba=new JLabel(); //存放选项
    private JLabel lbb=new JLabel();
    private JLabel lbc=new JLabel();
    private JLabel lbd=new JLabel();
    private JLabel timing =new JLabel();
    private JLabel fly=new JLabel();//答对后用于飞向单词的Label
   // private JLabel blood=new JLabel();
    private Socket s = null;
    private Timer timer = new Timer(100,this); //计时器任务  将初始延迟和事件间延迟初始化为 delay 毫秒
    private BufferedReader br = null;
    private PrintStream ps = null;
    private boolean canRun = true;
    PrintStream psf; //写文件
    public File file1;
    public File file2;
    public File file3;
    public String vocabulary;//储存要写入文本的单词
    public GamePanel(String nickName){// 构造器
        try {
            file1 = new File("/判断正确的单词表" + nickName);
            file1.createNewFile();
            file2 = new File("/判断错误的单词表" + nickName);
            file2.createNewFile();
            file3 = new File("/未判断的单词表" + nickName);
            file3.createNewFile();
        }catch (Exception ex){
        }
        this.setLayout(null);
        this.setBackground(Color.DARK_GRAY);
        this.setSize(600,600);
        this.add(lbLife);
        lbLife.setFont(new Font("黑体",Font.BOLD,20));
        //lbLife.setBackground(Color.yellow);
        lbLife.setForeground(Color.PINK);
        lbLife.setBounds(0,0,this.getWidth(),20);
        lbLife.setText("当前生命值:" + life); //可能出错
        timing.setFont(new Font("黑体",Font.BOLD,50));
        timing.setForeground(Color.red);
        timing.setBounds(this.getWidth()/2,this.getHeight()/2-60,60,60);
        this.add(timing);
        this.add(lbword);
        lbword.setFont(new Font("黑体",Font.BOLD,20));
        lbword.setForeground(Color.white);
        this.add(lba);
        //lba.setLayout(null);
        lba.setFont(new Font("黑体",Font.BOLD,16));
        lba.setBounds(0,this.getHeight()-40,this.getWidth()/4,20);
        lba.setForeground(Color.white);
        this.add(lbb);
        //lbb.setLayout(null);
        lbb.setFont(new Font("黑体",Font.BOLD,16));
        lbb.setBounds(this.getWidth()/4,this.getHeight()-40,this.getWidth()/4,20);
        lbb.setForeground(Color.white);
        this.add(lbc);
        //lbc.setLayout(null);
        lbc.setFont(new Font("黑体",Font.BOLD,16));
        lbc.setBounds(this.getWidth()/2,this.getHeight()-40,this.getWidth()/4,20);
        lbc.setForeground(Color.white);
        this.add(lbd);
        //lbd.setLayout(null);
        lbd.setFont(new Font("黑体",Font.BOLD,16));
        lbd.setBounds((this.getWidth()/4)*3,this.getHeight()-40,this.getWidth()/4,20);
        lbd.setForeground(Color.white);
        this.add(fly);
        fly.setFont(new Font("黑体",Font.BOLD,20));
        fly.setForeground(Color.white);
        /*this.add(blood);
        blood.setOpaque(true);
        blood.setBackground(Color.pink);
        blood.setBounds(0,25,200,10);*/
        //以上为abcd选项，生命值，掉落单词参数设置
        this.addKeyListener(this);

        try {
            s = new Socket("127.0.0.1", 9999);
            //JOptionPane.showMessageDialog(this,"连接成功");
            InputStream is = s.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            OutputStream os = s.getOutputStream();
            ps = new PrintStream(os);
            new Thread(this).start();
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this,"游戏异常退出！");
            System.exit(0);
        }
        timer.start();//实现下落
    }
    public void run() {
        try {
            while (canRun) {
                String str = br.readLine();  // 从socket读分数是加还是减,还要读取游戏内容
                if(str.equals("-1")||str.equals("1")) {
                    int score = Integer.parseInt(str);
                    life += score;
                    checkFail();
                }
                else {
                    if(str.equals("请等待另一玩家加入……")){
                        JOptionPane.showMessageDialog(this,str);
                    }
                    else {
                        if(str.equals("start")){
                            for(int i=3;i>0;i--){
                                timing.setText(new String().valueOf(i));
                                Thread.sleep(500);
                            }
                            timing.setText(null);
                        }
                        else {
                            String[] temp = str.split("#");
                            if(flag==0){
                                filewriter(file3);
                            }           //若未答题写入未判断的文件
                            flag=0;
                            lbword.setText(temp[0]);
                            fly.setText("");
                            lbword.setBounds(getWidth()/2, 0, 400,30);
                            answer = temp[1];
                            vocabulary=temp[0]+"\t\t\t"+answer;
                            lba.setText("A  " + temp[2]);
                            lbb.setText("B  " + temp[3]);
                            lbc.setText("C  " + temp[4]);
                            lbd.setText("D  " + temp[5]);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            canRun = false;
            javax.swing.JOptionPane.showMessageDialog(this," 游戏异常退出！");
            System.exit(0);
        }
    }
    // Timer 事件对应的行为:实现掉下一个字
    public void actionPerformed(ActionEvent e){ //掉下之前没有接到，就扣分
        try {
            if(lbword.getY()>=this.getHeight()) {
                 filewriter(file3);
                 flag=1;//
                 ps.println("超时"); //checkfile 可以初始化
               // Thread.sleep(100); //等待新的内容传输
                //System.out.println("chaoshi");
                checkFail();  //如果没死，就生成一个新的字母
                //lbLife.setText("当前生命值:" + life);
        }
      //  if(flag==1) {
            lbword.setLocation(lbword.getX(), lbword.getY() + 5);//移动rectangle,字母掉下
        //}
        /*else{
            switch (temp){
                case 'a':move(lba);break;
                case 'b':move(lbb);break;
                case 'c':move(lbc);break;
                case 'd':move(lbd);break;
            }
        }*/
        }catch (Exception ex){ }
    }
    public void checkFail()throws Exception{
        lbLife.setText("当前生命值:" + life);
        if(life<=0){
            //lbLife.setText("当前生命值:" + life);//死的时候0
            //Thread.sleep(50);
            lbLife.setText("当前生命值:" + 0);
            timer.stop();
            javax.swing.JOptionPane.showMessageDialog(this, "生命值耗尽，游戏结束！");
            System.exit(0);
        }

    }
    //  键盘操作事件对应的行为
    public void keyPressed(KeyEvent e){
        try{
               keychar=e.getKeyChar();
               flag=1;
               switch (keychar){
                   case 'a':checkanswer(lba);break;
                   case 'b':checkanswer(lbb);break;
                   case 'c':checkanswer(lbc);break;
                   case 'd':checkanswer(lbd);break;
                   /*default: {
                       ps.println("+1");
                       life -=2;
                   }*/
               }
            checkFail();
        }catch(Exception ex){
            ex.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this,"  游戏异常退出！");
            System.exit(0);
        } }
    public void keyTyped(KeyEvent e){}
    public void keyReleased(KeyEvent e){}
    public  void checkanswer(JLabel j) throws Exception{


        if(j.getText().endsWith(answer)){ //注意，这里加 2 分，然后发 “-1”给所有用户，本用户也会收到，结果为加 1 分
             //传给服务器，对方生命值-1
            move(j);
            life += 2;
            filewriter(file1);
           // fly.setText(j.getText());
           // j.setText("");
            //fly.setBounds(lbword.getX(),lbword.getY()+30,getWidth()/4,20);
            //flag=0;
            //j.setLocation(lbword.getX(), lbword.getY()+30 );
            //temp=' ';
        /*while(j.getX()!=lbword.getX()||j.getY()>lbword.getY()+20) {
            //j.setLocation(j.getX() + (lbword.getX() - a) / 10, j.getHeight() - (this.getHeight() - j.getY()) / 10);
            j.setLocation(j.getX() + 5, j.getHeight() - 5);
            Thread.sleep(50);
        }*/


            //j.setLocation(a,b);
            ps.println("-1");
            //Thread.sleep(100);
        }else{
            //flag=0;
            //lbword.setLocation(lbword.getX(),lbword.getY());
              //答错扣分
            move(j);
            life-=2;
            filewriter(file2);
            ps.println("1");
            //flag=1;
            //System.out.println(answer+life);
        }
    }
    public void filewriter(File file) throws Exception{
        psf=new PrintStream(new FileOutputStream(file.getPath(),true)); //总是覆盖
        psf.println(vocabulary);
        psf.close();
    }
    public void move(JLabel j)throws Exception{
        fly.setText(j.getText());
        j.setText("");
        fly.setBounds(lbword.getX(),lbword.getY()+30,getWidth()/4,20);
        Thread.sleep(1000);
        //flag=0;
        //j.setLocation(lbword.getX(), lbword.getY()+30 );
        //temp=' ';
        /*while(j.getX()!=lbword.getX()||j.getY()>lbword.getY()+20) {
            //j.setLocation(j.getX() + (lbword.getX() - a) / 10, j.getHeight() - (this.getHeight() - j.getY()) / 10);
            j.setLocation(j.getX() + 5, j.getHeight() - 5);
            Thread.sleep(50);
        }*/

    }
}
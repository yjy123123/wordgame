import javax.swing.*;

public class GameFrame extends JFrame{
    private GamePanel gp;
    //color
    /*public JMenuBar jmb=new JMenuBar();
    public JMenu mbg=new JMenu("背景颜色");
    public JMenu mfg=new JMenu("文字颜色");
    public JRadioButtonMenuItem  black1=new JRadioButtonMenuItem("黑色");
    public JRadioButtonMenuItem  black2=new JRadioButtonMenuItem("黑色");
    public JRadioButtonMenuItem  white1=new JRadioButtonMenuItem("白色");
    public JRadioButtonMenuItem  white2=new JRadioButtonMenuItem("白色");
    public JRadioButtonMenuItem  custom1=new JRadioButtonMenuItem("自定义");
    public JRadioButtonMenuItem  custom2=new JRadioButtonMenuItem("自定义");*/
    public GameFrame(){//构造器
        /*mbg.add(black1);
        mfg.add(black2);
        mbg.add(white1);
        mfg.add(white2);
        mbg.add(custom1);
        mfg.add(custom2);
        jmb.add(mbg);
        jmb.add(mfg);
        this.setJMenuBar(jmb);*/
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String nickName = JOptionPane.showInputDialog("输入昵称");
        this.setTitle(nickName);
        gp = new GamePanel(nickName);
        this.add(gp);
        //获取焦点
        gp.setFocusable(true);  //鼠标指向  有了才能用键盘
        this.setSize(gp.getWidth(), gp.getHeight());
        this.setResizable(false);  //不能自己决定窗口大小，固定 否则不公平，掉落时间不同
        this.setVisible(true);
    }
    //主函数入口:
    public static void main(String[] args){
        new GameFrame();
    }
}

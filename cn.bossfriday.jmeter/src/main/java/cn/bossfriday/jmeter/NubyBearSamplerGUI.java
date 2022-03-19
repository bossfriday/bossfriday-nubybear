package cn.bossfriday.jmeter;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledChoice;

import javax.swing.*;
import java.awt.*;

import static cn.bossfriday.jmeter.common.Const.*;

public class NubyBearSamplerGUI extends AbstractSamplerGui {
    private JTextField systemName;
    private JTextField zkAddress;

    private JTextField rpcHost;
    private JTextField rpcPort;

    private JTextField nodeName;
    private JTextField virtualNodesNum;

    private JLabeledChoice behaviorName;

    public NubyBearSamplerGUI() {
        super();

        JPanel settingPanel = new VerticalPanel(10, 0);
        settingPanel.add(getBlank());
        settingPanel.add(createSystemComponent());
        settingPanel.add(createRpcAddress());
        settingPanel.add(createNode());
        settingPanel.add(getBehaviorName());

        JPanel dataPanel = new JPanel(new BorderLayout(5, 0));
        dataPanel.add(settingPanel, BorderLayout.NORTH);
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH); // Add the standard title
        add(dataPanel, BorderLayout.CENTER);
    }

    @Override
    public String getLabelResource() {
        throw new IllegalStateException("This shouldn't be called");
    }

    /**
     * 创建一个新的Sampler，然后将界面中的数据设置到这个新的Sampler实例中
     */
    @Override
    public TestElement createTestElement() {
        NubyBearSampler sampler = new NubyBearSampler();
        modifyTestElement(sampler);

        return sampler;
    }

    /**
     * 把界面的数据移到Sampler中，与configure方法相反
     */
    @Override
    public void modifyTestElement(TestElement testElement) {
        testElement.clear();
        configureTestElement(testElement);

        testElement.setProperty(GUID_SYSTEM_NAME, systemName.getText());
        testElement.setProperty(GUID_NODE_NAME, nodeName.getText());
        testElement.setProperty(GUID_ZK_ADDRESS, zkAddress.getText());
        testElement.setProperty(GUID_HOST, rpcHost.getText());
        testElement.setProperty(GUID_PORT, rpcPort.getText());
        testElement.setProperty(GUID_VIRTUAL_NODES_NUM, virtualNodesNum.getText());
        testElement.setProperty(GUI_BEHAVIOR_NAME, behaviorName.getText());

    }

    @Override
    public String getStaticLabel() {
        return SAMPLER_NAME;
    }

    /**
     * reset新界面的时候调用，这里可以填入界面控件中需要显示的一些缺省的值
     */
    @Override
    public void clearGui() {
        super.clearGui();

        systemName.setText("foo");
        nodeName.setText("node1");
        zkAddress.setText("127.0.0.1:2181");
        rpcHost.setText("127.0.0.1");
        rpcPort.setText("18080");
        virtualNodesNum.setText("100");
        behaviorName.setText("");
    }

    /**
     * 把Sampler中的数据加载到界面中
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);

        systemName.setText(element.getPropertyAsString(GUID_SYSTEM_NAME));
        nodeName.setText(element.getPropertyAsString(GUID_NODE_NAME));
        zkAddress.setText(element.getPropertyAsString(GUID_ZK_ADDRESS));
        rpcHost.setText(element.getPropertyAsString(GUID_HOST));
        rpcPort.setText(element.getPropertyAsString(GUID_PORT));
        virtualNodesNum.setText(element.getPropertyAsString(GUID_VIRTUAL_NODES_NUM));
        behaviorName.setText(element.getPropertyAsString(GUI_BEHAVIOR_NAME));
    }

    protected Component getBlank() {
        JLabel label1 = new JLabel(" ");
        JPanel panel = new HorizontalPanel();
        panel.add(label1, BorderLayout.WEST);

        return panel;
    }

    protected Component createSystemComponent() {
        systemName = new JTextField(10);
        JLabel label1 = new JLabel("SystemName: ");
        label1.setLabelFor(systemName);

        zkAddress = new JTextField(20);
        JLabel label2 = new JLabel("    ZkAddress: ");
        label2.setLabelFor(zkAddress);


        JPanel panel = new HorizontalPanel();
        panel.setLayout(new FlowLayout(0, 0, 0));
        panel.add(label1, BorderLayout.WEST);
        panel.add(systemName, BorderLayout.WEST);
        panel.add(label2, BorderLayout.WEST);
        panel.add(zkAddress, BorderLayout.WEST);

        return panel;
    }

    protected Component createRpcAddress() {
        rpcHost = new JTextField(20);
        JLabel label1 = new JLabel("RpcHost: ");
        label1.setLabelFor(rpcHost);

        rpcPort = new JTextField(10);
        JLabel label2 = new JLabel("    RpcPort: ");
        label2.setLabelFor(rpcPort);

        JPanel panel = new HorizontalPanel();
        panel.setLayout(new FlowLayout(0, 0, 0));
        panel.add(label1, BorderLayout.WEST);
        panel.add(rpcHost, BorderLayout.WEST);
        panel.add(label2, BorderLayout.WEST);
        panel.add(rpcPort, BorderLayout.WEST);

        return panel;
    }

    protected Component createNode() {
        nodeName = new JTextField(20);
        JLabel label1 = new JLabel("NodeName: ");
        label1.setLabelFor(nodeName);

        virtualNodesNum = new JTextField(10);
        JLabel label2 = new JLabel("    VirtualNodesNum: ");
        label2.setLabelFor(virtualNodesNum);

        JPanel panel = new HorizontalPanel();
        panel.setLayout(new FlowLayout(0, 0, 0));
        panel.add(label1, BorderLayout.WEST);
        panel.add(nodeName, BorderLayout.WEST);
        panel.add(label2, BorderLayout.WEST);
        panel.add(virtualNodesNum, BorderLayout.WEST);

        return panel;
    }

    protected Component getBehaviorName() {
        behaviorName = new JLabeledChoice("BehaviorName:", NubyBearSamplerBuilder.behaviorNames, true, false);

        JPanel panel = new HorizontalPanel();
        panel.setLayout(new FlowLayout(0, 0, 0));
        panel.add(behaviorName, BorderLayout.WEST);

        return panel;
    }
}

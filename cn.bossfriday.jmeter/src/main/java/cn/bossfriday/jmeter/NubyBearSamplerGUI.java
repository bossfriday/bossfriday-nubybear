package cn.bossfriday.jmeter;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledChoice;

import javax.swing.*;
import java.awt.*;

import static cn.bossfriday.jmeter.common.Const.GUI_BEHAVIOR_NAME;
import static cn.bossfriday.jmeter.common.Const.SAMPLER_NAME;

public class NubyBearSamplerGUI extends AbstractSamplerGui {
    private JLabeledChoice behaviorName;

    public NubyBearSamplerGUI() {
        super();

        JPanel settingPanel = new VerticalPanel(10, 0);
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

        behaviorName.setText("");
    }

    /**
     * 把Sampler中的数据加载到界面中
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);

        behaviorName.setText(element.getPropertyAsString(GUI_BEHAVIOR_NAME));
    }

    protected Component getBlank() {
        JLabel label1 = new JLabel(" ");
        JPanel panel = new HorizontalPanel();
        panel.add(label1, BorderLayout.WEST);

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

package microtrafficsim.ui.preferences;

import microtrafficsim.core.simulation.controller.configs.SimulationConfig;
import microtrafficsim.ui.preferences.impl.*;

import javax.swing.*;
import java.awt.*;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class PrefPanel extends JPanel implements IPreferences {

    protected final SimulationConfig config;
    public final String title;
    private int gridx, gridy;

    public PrefPanel(SimulationConfig config, String title) {
        super(new GridBagLayout());
        this.config = config;
        this.title = title;
        gridy = -1;
    }

    /**
     * @return this
     */
    public abstract void setAllEnabled(boolean enabled);
    public abstract void setEnabled(boolean enabled, ComponentId component);

    /**
     * also resets gridx (columns)
     */
    public void incRow() {
        gridx = -1;
        gridy++;
    }

    /**
     * also resets gridx (columns)
     */
    public void decRow() {
        gridx = -1;
        gridy--;
    }

    public void incColumn() {
        gridx++;
    }

    /**
     * resets<br>
     * &bull constraints.gridx to current gridx<br>
     * &bull constraints.gridy to current gridy<br>
     * &bull constraints.insets to (top, left, bottom, right) = (0, 5, 0, 5)
     *
     * @param component
     * @param constraints
     */
    public void add(JComponent component, GridBagConstraints constraints) {
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.insets = new Insets(0, 5, 0, 5);
        super.add(component, constraints);
    }

    protected void configureAndAddJTextFieldRow(String labelText, JTextField textField) {

        incRow();

        /* components */
        textField.setColumns(14);

        /* JLabel */
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 0;
        constraints.anchor = GridBagConstraints.WEST;
        JLabel label = new JLabel(labelText);
        label.setFont(PrefFrame.TEXT_FONT);
        incColumn();
        add(label, constraints);

        /* JTextField */
        textField.setHorizontalAlignment(JTextField.RIGHT);
        constraints = new GridBagConstraints();
        constraints.weightx = 0;
        constraints.anchor = GridBagConstraints.WEST;
        incColumn();
        add(textField, constraints);

        incColumn();
        addGapPanel(1);
    }

    public void addGapPanel(double weightx) {
        addGapPanel(weightx, 1);
    }

    public void addGapPanel(double weightx, int gridwidth) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = weightx;
        constraints.gridwidth = gridwidth;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        add(new JPanel(), constraints);
    }

    /*
    |==================|
    | (i) IPreferences |
    |==================|
    */
    @Override
    public void addListener(PreferencesListener listener) {
        // empty
    }
}
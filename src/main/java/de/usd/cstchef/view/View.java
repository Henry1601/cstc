package de.usd.cstchef.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.security.Security;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import burp.BurpUtils;
import de.usd.cstchef.view.filter.FilterState;
import de.usd.cstchef.view.filter.FilterState.BurpOperation;
import de.usd.cstchef.view.ui.NotificationMenu;

public class View extends JPanel {

    private RecipePanel incomingRecipePanel;
    private RecipePanel outgoingRecipePanel;
    private RecipePanel formatRecipePanel;

    private NotificationMenu cstcMenu = new NotificationMenu("CSTC", false);
    private Color defaultMenuForeground;

    public View(){
        this(new FilterState());
    }

    public View(FilterState state) {
        Security.addProvider(new BouncyCastleProvider());

        this.setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();

        incomingRecipePanel = new RecipePanel(BurpOperation.INCOMING);
        outgoingRecipePanel = new RecipePanel(BurpOperation.OUTGOING);
        formatRecipePanel = new RecipePanel(BurpOperation.FORMAT);

        tabbedPane.addTab("Outgoing Requests", null, outgoingRecipePanel, "Outgoing requests from the browser, the repeater or another tool.");
        tabbedPane.addTab("Incoming Responses", null, incomingRecipePanel, "Responses from the server.");
        tabbedPane.addTab("Formatting", null, formatRecipePanel, "Formatting for messages.");
        this.add(tabbedPane);

        Object[] options = { "Close" };
        JMenuItem openFilterDialogItem = new JMenuItem("Filter Dialog");
        openFilterDialogItem.addActionListener(e -> JOptionPane.showOptionDialog(null, RequestFilterDialog.getInstance(), "Request Filter",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]));

        cstcMenu.add(openFilterDialogItem);
        defaultMenuForeground = cstcMenu.getForeground();

        BurpUtils.getInstance().getApi().userInterface().menuBar().registerMenu(cstcMenu);
    }

    public RecipePanel getIncomingRecipePanel() {
        return this.incomingRecipePanel;
    }

    public RecipePanel getOutgoingRecipePanel() {
        return this.outgoingRecipePanel;
    }

    public RecipePanel getFormatRecipePanel() {
        return this.formatRecipePanel;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("CSTC");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        View view = new View();

        frame.setContentPane(view);
        frame.setSize(800, 600);
        frame.setVisible(true);
//        frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
    }

    public void updateInactiveWarnings() {
        incomingRecipePanel.showInactiveWarning();
        boolean filterActive = false;

        for(Boolean b : BurpUtils.getInstance().getFilterState().getIncomingFilterSettings().values()){
            if(b == true) {
                incomingRecipePanel.hideInactiveWarning();
                filterActive = true;
            }
        }

        outgoingRecipePanel.showInactiveWarning();
        for(Boolean b : BurpUtils.getInstance().getFilterState().getOutgoingFilterSettings().values()){
            if(b == true) {
                outgoingRecipePanel.hideInactiveWarning();
                filterActive = true;
            }
        }

        cstcMenu.setText("CSTC");
        cstcMenu.setNotificationVisible(filterActive);
        cstcMenu.setForeground(filterActive ? new Color(0xff6633) : defaultMenuForeground);
    }

    public void preventRaceConditionOnVariables() {
        incomingRecipePanel.disableAutobakeIfFilterActive();
        outgoingRecipePanel.disableAutobakeIfFilterActive();
        formatRecipePanel.disableAutobakeIfFilterActive();
    }
}

package wb_demo;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import swing2swt.layout.BorderLayout;

public class InstallationWindow extends Composite {
	private Label installationHeaderTitle;
	private Label installationMotorType;
	private Label installationConnectionInfo;
	private Label installationConnectionStatus;
	private Label installationWarning;
	private Composite installationConnectOptionsComposite;
	private Label installationMotorStatus;
	private Label installationConnectButton;
	private Label installationConnectStatusVariable;
	private Composite installationMotorConfigurationComposite;
	private Composite installationConnectStatusComposite;
	private Composite installationWarningComposite;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public InstallationWindow(Composite parent, int style) {
		super(parent, SWT.NONE);
		setLayout(new BorderLayout(0, 0));
		
		installationHeaderTitle = new Label(this, SWT.BORDER);
		installationHeaderTitle.setText("<installationHeaderTitle>");
		installationHeaderTitle.setLayoutData(BorderLayout.NORTH);
		
		Composite installationEntryFieldsComposite = new Composite(this, SWT.NONE);
		installationEntryFieldsComposite.setLayoutData(BorderLayout.WEST);
		installationEntryFieldsComposite.setLayout(new RowLayout(SWT.VERTICAL));
		
		installationMotorConfigurationComposite = new Composite(installationEntryFieldsComposite, SWT.NONE);
		installationMotorConfigurationComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		installationMotorType = new Label(installationMotorConfigurationComposite, SWT.BORDER);
		installationMotorType.setText("<installationMotorType>");
		
		installationMotorStatus = new Label(installationMotorConfigurationComposite, SWT.BORDER);
		installationMotorStatus.setText("<installationMotorStatus>");
		
		installationConnectOptionsComposite = new Composite(installationEntryFieldsComposite, SWT.NONE);
		installationConnectOptionsComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		installationConnectionInfo = new Label(installationConnectOptionsComposite, SWT.BORDER);
		installationConnectionInfo.setText("<installationConnectionInfo>");
		
		installationConnectButton = new Label(installationConnectOptionsComposite, SWT.NONE);
		installationConnectButton.setText("<installationConnectButton>");
		
		installationConnectStatusComposite = new Composite(installationEntryFieldsComposite, SWT.NONE);
		installationConnectStatusComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		installationConnectionStatus = new Label(installationConnectStatusComposite, SWT.BORDER);
		installationConnectionStatus.setText("<installationConnectionStatus>");
		
		installationConnectStatusVariable = new Label(installationConnectStatusComposite, SWT.BORDER);
		installationConnectStatusVariable.setText("<installationConnectStatusVariable>");
		
		installationWarningComposite = new Composite(installationEntryFieldsComposite, SWT.NONE);
		installationWarningComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		installationWarning = new Label(installationWarningComposite, SWT.BORDER);
		installationWarning.setText("<installationWarning>");

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

package wb_demo;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import swing2swt.layout.BorderLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;

public class ProgramWindow extends Composite {
	private Label programHeaderTitle;
	private Label programPositionInfo;
	private Text programPositionField;
	private Text programVelocityField;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public ProgramWindow(Composite parent, int style) {
		super(parent, style);
		setLayout(new BorderLayout(0, 0));
		
		Composite DataComposite = new Composite(this, SWT.NONE);
		DataComposite.setLayoutData(BorderLayout.WEST);
		DataComposite.setLayout(new RowLayout(SWT.VERTICAL));
		
		Label programHomeInfo = new Label(DataComposite, SWT.NONE);
		programHomeInfo.setText("<programHomeInfo>");
		
		Composite programPositionComposite = new Composite(DataComposite, SWT.NONE);
		programPositionComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		programPositionInfo = new Label(programPositionComposite, SWT.BORDER);
		programPositionInfo.setText("<programPositionInfo>");
		
		Composite composite = new Composite(programPositionComposite, SWT.NONE);
		composite.setLayout(new RowLayout(SWT.VERTICAL));
		
		Composite composite_1 = new Composite(composite, SWT.NONE);
		composite_1.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		Label programPositionLabel = new Label(composite_1, SWT.NONE);
		programPositionLabel.setText("<programPositionLabel>");
		
		programPositionField = new Text(composite_1, SWT.BORDER);
		programPositionField.setText("<programPositionField>");
		
		Composite composite_2 = new Composite(composite, SWT.NONE);
		composite_2.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		Label programVelocityLabel = new Label(composite_2, SWT.NONE);
		programVelocityLabel.setText("<programVelocityLabel>");
		
		programVelocityField = new Text(composite_2, SWT.BORDER);
		programVelocityField.setText("<programVelocityField>");
		
		Composite programJogComposite = new Composite(DataComposite, SWT.NONE);
		programJogComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		Label programJogInfo = new Label(programJogComposite, SWT.NONE);
		programJogInfo.setText("<programJogInfo>");
		
		Button programJogStartButton = new Button(programJogComposite, SWT.NONE);
		programJogStartButton.setText("<programJogStartButton>");
		
		Button programJogStopButton = new Button(programJogComposite, SWT.NONE);
		programJogStopButton.setText("<programJogStopButton>");
		
		Composite composite_3 = new Composite(DataComposite, SWT.NONE);
		composite_3.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		Label programVerifyPositionInfo = new Label(composite_3, SWT.NONE);
		programVerifyPositionInfo.setText("<programVerifyPositionInfo>");
		
		Label programVerifyPositionField = new Label(composite_3, SWT.RIGHT);
		programVerifyPositionField.setAlignment(SWT.RIGHT);
		programVerifyPositionField.setText("<programVerifyPositionField>");
		
		Composite HeaderComposite = new Composite(this, SWT.NONE);
		HeaderComposite.setLayoutData(BorderLayout.NORTH);
		RowLayout rl_HeaderComposite = new RowLayout(SWT.VERTICAL);
		rl_HeaderComposite.fill = true;
		rl_HeaderComposite.center = true;
		HeaderComposite.setLayout(rl_HeaderComposite);
		
		programHeaderTitle = new Label(HeaderComposite, SWT.BORDER | SWT.CENTER);
		programHeaderTitle.setText("<programHeaderTitle>");
		
		Label programHeaderInfo = new Label(HeaderComposite, SWT.NONE);
		programHeaderInfo.setText("<programHeaderInfo>");

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}

/**
 * 
 */
package net.rrm.ehour.ui.audit.panel;

import net.rrm.ehour.ui.border.GreyRoundedBorder;
import net.rrm.ehour.ui.panel.AbstractFormSubmittingPanel;

import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.model.IModel;

/**
 * @author thies
 *
 */
public class AuditReportCriteriaPanel extends AbstractFormSubmittingPanel
{
	private static final long serialVersionUID = -5442954150653475254L;

	public AuditReportCriteriaPanel(String id, IModel model)
	{
		super(id, model);
		
		addComponents();
	}
	
	private void addComponents()
	{
		Border greyBorder = new GreyRoundedBorder("border", 500);
		add(greyBorder);
	}
}
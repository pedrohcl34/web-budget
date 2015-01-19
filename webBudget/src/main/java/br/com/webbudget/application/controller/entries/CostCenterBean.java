package br.com.webbudget.application.controller.entries;

import br.com.webbudget.application.controller.AbstractBean;
import br.com.webbudget.application.exceptions.ApplicationException;
import br.com.webbudget.domain.entity.movement.CostCenter;
import br.com.webbudget.domain.service.MovementService;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

/**
 *
 * @author Arthur Gregorio
 *
 * @version 1.0
 * @since 1.0, 04/03/2014
 */
@ViewScoped
@ManagedBean
public class CostCenterBean extends AbstractBean {

    @Getter
    private CostCenter costCenter;
    @Getter
    private List<CostCenter> costCenters;
    
    @Setter
    @ManagedProperty("#{movementService}")
    private MovementService movementService;

    /**
     * 
     * @return 
     */
    @Override
    protected Logger initializeLogger() {
        return LoggerFactory.getLogger(CostCenterBean.class);
    }
    
    /**
     * 
     */
    public void initializeListing(){
        if (!FacesContext.getCurrentInstance().isPostback()) {
            this.viewState = ViewState.LISTING;
            this.costCenters = this.movementService.listCostCenters(null);
        }
    }

    /**
     * 
     * @param costCenterId 
     */
    public void initializeForm(long costCenterId) {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            
            this.costCenters = this.movementService.listCostCenters(false);
            
            if (costCenterId == 0) {
                this.viewState = ViewState.ADDING;
                this.costCenter = new CostCenter();
            } else {
                this.viewState = ViewState.EDITING;
                this.costCenter = this.movementService.findCostCenterById(costCenterId);
            }
        }
    }
    
    /**
     * 
     * @return
     */
    public String changeToAdd() {
        return "formCostCenter.xhtml?faces-redirect=true";
    }
    
    /**
     * 
     * @return 
     */
    public String changeToListing() {
        return "listCostCenters.xhtml?faces-redirect=true";
    }
    
    /**
     * 
     * @param costCenterId
     * @return 
     */
    public String changeToEdit(long costCenterId) {
        return "formCostCenter.xhtml?faces-redirect=true&costCenterId=" + costCenterId;
    }
    
    /**
     * 
     * @param costCenterId 
     */
    public void changeToDelete(long costCenterId) {
        this.costCenter = this.movementService.findCostCenterById(costCenterId);
        this.openDialog("deleteCostCenterDialog" , "dialogDeleteCostCenter");
    }

    /**
     * Cancela e volta para a listagem
     * 
     * @return 
     */
    public String doCancel() {
        return "listCostCenters.xhtml?faces-redirect=true";
    }
    
    /**
     * 
     */
    public void doSave() {
        
        try {
            this.movementService.saveCostCenter(this.costCenter);
            this.costCenter = new CostCenter();
            
            // busca novamente os centros de custo para atualizar a lista de parentes
            this.costCenters = this.movementService.listCostCenters(false);
            
            this.info("cost-center.action.saved", true);
        }  catch (ApplicationException ex) {
            this.logger.error("CostCenterBean#doSave found erros", ex);
            this.fixedError(ex.getMessage(), true);
        } 
    }
    
    /**
     * 
     */
    public void doUpdate() {
        
        try {
            this.costCenter = this.movementService.updateCostCenter(this.costCenter);
            
            this.info("cost-center.action.updated", true);
        } catch (ApplicationException ex) {
            this.logger.error("CostCenterBean#doUpdate found erros", ex);
            this.fixedError(ex.getMessage(), true);
        } 
    }
    
    /**
     * 
     */
    public void doDelete() {
        
        try {
            this.movementService.deleteCostCenter(this.costCenter);
            this.costCenters = this.movementService.listCostCenters(false);
            
            this.info("cost-center.action.deleted", true);
        } catch (DataIntegrityViolationException ex) {
            this.logger.error("CostCenterBean#doDelete found erros", ex);
            this.fixedError("cost-center.action.delete-used", true);
        } catch (Exception ex) {
            this.logger.error("CostCenterBean#doDelete found erros", ex);
            this.fixedError(ex.getMessage(), true);
        } finally {
            this.closeDialog("dialogDeleteCostCenter");
            this.update("costCentersList");
        }
    }
}

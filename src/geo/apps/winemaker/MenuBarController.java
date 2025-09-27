package geo.apps.winemaker;

import java.net.URL;
import java.util.ResourceBundle;

import geo.apps.winemaker.utilities.HelperFunctions;
import geo.apps.winemaker.utilities.Registry;
import geo.apps.winemaker.utilities.Constants.RegistryKeys;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

public class MenuBarController implements Initializable {

	private WineMakerController wineMakerController;
	private WineMakerModel winemakerModel;
	
    public MenuBarController()
    {   }
	    
	@FXML
	private void exportBatchButton(ActionEvent e)
	{
    	this.wineMakerController.exportExistingBatch(e);
	}

	@FXML
	private void reportBatchButton(ActionEvent e)
	{
    	this.wineMakerController.reportExistingBatch(e);
	}

	@FXML
	private void exportAllBatches(ActionEvent e)
	{
    	this.wineMakerController.exportAllBatches(e);
	}
	
	@FXML
	public void sayGoodbye(ActionEvent e)
	{
		this.wineMakerController.sayGoodbye(e);
	}

	@FXML
	public void updateCodesTable(ActionEvent e)
	{
		this.wineMakerController.updateCodesTable(e);
	}
	
	@FXML
	private void exportCodesFile(ActionEvent e)
	{
		this.wineMakerController.exportCodesFile(e);
	}
	
	@FXML
	private void loadCodes(ActionEvent e)
	{
		this.wineMakerController.loadCodes(e);
	}
	
	@FXML void openInventoryManagement(ActionEvent e)
	{
		this.wineMakerController.openInventoryManagement(e);
	}
	
	@FXML void exportInventory(ActionEvent e)
	{
		this.wineMakerController.exportInventory(e);
	}
	
	@FXML void loadInventory(ActionEvent e)
	{
		this.wineMakerController.loadInventory(e);
	}
	
	@FXML void reportInventory(ActionEvent e)
	{
		this.wineMakerController.reportInventory(e);
	}
	
	@FXML void backUpDatabase(ActionEvent e)
	{
		this.wineMakerController.backUpDatabase(e);
	}
	
	@FXML void restoreDatabase(ActionEvent e)
	{
		this.wineMakerController.restoreDatabase(e);
	}
	
	@FXML void moveDatabase(ActionEvent e)
	{
		this.wineMakerController.moveDatabase(e);
	}
	
	@FXML void tableReset(ActionEvent e)
	{
		this.wineMakerController.tableReset(e);
	}
	
	@FXML void exportLogFile(ActionEvent e)
	{
		this.wineMakerController.exportLogFile(e);
	}
	
	@FXML void showUserGuide(ActionEvent e)
	{
		this.wineMakerController.showUserGuide(e);
	}
	
	@FXML void showAbout(ActionEvent e)
	{
		this.wineMakerController.showAbout(e);
	}
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) 
	{
		Registry appRegistry = HelperFunctions.getRegistry();
		this.winemakerModel = (WineMakerModel) appRegistry.get(RegistryKeys.MODEL);
		this.wineMakerController = this.winemakerModel.getWinemakerController();
	}
}

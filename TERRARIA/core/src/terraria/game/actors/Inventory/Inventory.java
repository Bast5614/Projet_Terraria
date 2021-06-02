package terraria.game.actors.Inventory;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import terraria.game.TerrariaGame;
import terraria.game.actors.world.TileType;

public class Inventory extends Actor {

    private TerrariaGame game;
    private static final int SLOTINVENTORYBAR = 10;     //Nombre d'objet dans la barre d'inventaire
    private static final int SIZEINVENTORY = 50;        //Nombre d'objet total de l'inventaire
    private static int currentItems = 0;                //Numéro de l'items actuellement sélectionné
    private ArrayList<Items> itemsList;                 //La liste des objets de l'inventaire
    private ArrayList<ItemsGraphic> itemsGraphic;       //La liste la classe qui gère les textures des objets de l'inventaire
    private ArrayList<Items> countItems;
    private ArrayList<Items> craftableItemList;
    private ArrayList<ItemsGraphic> craftableItemGraphicList;
    private boolean inventoryShow;                      //Boolean qui détermine si l'inventaire est affiché ou non
    private TextureRegion[][] slot;                     //Texture de chaque slot d'inventaire
    private TextureRegion[][] hoverTexture;                  //Texture quand on passe la souris sur un slot en drag & drop

    private DragAndDrop dragAndDrop;                    //Drag & Drop de l'inventaire
    private float ScreenX, ScreenY,ScreenWidth,ScreenHeight;     //Taille de l'écran
    private int  width = 50, height = 50;                //Taille d'un slot

    Vector3 cam;

    public Inventory(TerrariaGame game) {
        this.game = game;
        this.itemsList = new ArrayList<>();
        this.itemsGraphic = new ArrayList<>();
        this.countItems = new ArrayList<>();
        this.craftableItemList = new ArrayList<>();
        this.craftableItemGraphicList = new ArrayList<>();
        this.dragAndDrop = new DragAndDrop();
        this.inventoryShow = false;
        this.slot = TextureRegion.split(game.getAssetManager().get("inventory/slot.png", Texture.class), width, height);
        this.hoverTexture = TextureRegion.split(game.getAssetManager().get("inventory/hover.png", Texture.class), width, height);
        //On crée les items de l'inventaire
        for (int i = 0; i < SIZEINVENTORY; i++) {
            itemsList.add(new Items(i));
            itemsGraphic.add(new ItemsGraphic(game, itemsList.get(i), this, dragAndDrop));
        }
        for (int i = 0; i < 50; i ++) {
            countItems.add(new Items(i, i));
        }
        countItems();
    }

    public void update(Camera camera, Stage stage){
        cam = camera.position;
        ScreenX =  cam.x + stage.getViewport().getScreenWidth()/2 - SLOTINVENTORYBAR * width;
        ScreenY = cam.y - stage.getViewport().getScreenHeight()/2;
        ScreenWidth =   stage.getViewport().getScreenWidth();
        ScreenHeight = stage.getViewport().getScreenHeight();

        /*System.out.println("debut");
        for(Items item : countItems) {
            System.out.println(TileType.getTileTypeById(item.getIdTile())+"  : "+item.getAmount());
        }
        System.out.println("fin");*/

        if (inventoryShow) {
            countItems();
            craftableItem();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        int nbItems = 0;
        //On dessine les slots de la barre d'inventaire
        for (int i = 0; i < SLOTINVENTORYBAR; i++){
            if (currentItems == i) {
                batch.draw(slot[0][1], ScreenX + width *  i - (width/2), ScreenY + ScreenHeight - (height + height/2));
            } else {
                batch.draw(slot[0][0], ScreenX + width *  i - (width/2), ScreenY + ScreenHeight - (height + height/2));
            }
            if (itemsGraphic.get(i).isHover())
                batch.draw(hoverTexture[0][0], ScreenX + width *  itemsGraphic.get(i).getYPosition() - (width/2), ScreenY + ScreenHeight - (itemsGraphic.get(i).getXPosition()*height+height+height/2));
            nbItems++;
        }

        //Si l'inventaire est affiché
        if (inventoryShow) {
            for (int i = 1; i < 5; i++) {
                for (int j = 0; j < SLOTINVENTORYBAR; j++) {
                    batch.draw(slot[0][0], ScreenX + width * j - (width / 2), ScreenY + ScreenHeight - (i*height + height + height / 2));
                    if (itemsGraphic.get(nbItems).isHover())
                        batch.draw(hoverTexture[0][0], ScreenX + width *  j - (width/2), ScreenY + ScreenHeight - (i*height+height+height/2));
                    nbItems++;
                }
            }

            //On dessine les slots de craft disponible
            for (int craftableItem = 0; craftableItem < craftableItemList.size(); craftableItem++) {
                batch.draw(slot[0][0], ScreenX - (width/2), ScreenY + ScreenHeight -  (6*height+height+height/2) - craftableItem*height);
            }
        }




    }

    /**
     * Ajoute un élément à un slot, s'il existe un slot contenant déjà un élément
     * du même type et qu'il y a moins de 64 éléments dans ce slot. Dans le cas contraire
     * on va chercher s'il existe un slot vide pouvant acceuilir cet élement. 
     * @param idTile
     * @return
     */
    public boolean addTileInInventory(int idTile) {
        for(Items t : itemsList) {
            if(idTile == t.getIdTile() && t.getAmount() < 64) {
                t.incrAmount();
                return true;
            }
        }
        for(Items t2 : itemsList) {
            if(t2.getIdTile() == 0) {
                t2.setIdTile(idTile);
                t2.incrAmount();
                return true;
            }
        }

        return false;
    }


    /**
     * Enlève un élément de l'inventaire et supprime complètement l'élément du slot
     * quand le nombre d'élement descend à 0.
     */
    public void delTileInInventory() {
        Items currentSlot = itemsList.get(currentItems);
        if(currentSlot.getAmount() == 1) {
            currentSlot.lastElement();
        } else {
            currentSlot.decrAmount();
        }
    }

    public void fillInventory(ArrayList<Items> inv) {
        int indice = 0;
        if (inv != null) {
            for(Items i : inv) {
                this.itemsList.set(indice, i);
                this.getGraphicItems().set(indice, new ItemsGraphic(game, this.itemsList.get(indice),this, dragAndDrop));
                indice++;
            }
        }
    }

    public int getCurrentItems() {
        return currentItems;
    }

    public static void setCurrentItems(int tile) {
        currentItems = tile;
    }

    public ArrayList<Items> getItemsList() {
        return itemsList;
    }

    public ArrayList<ItemsGraphic> getGraphicItems() {
        return itemsGraphic;
    }

    public boolean isInventoryOpen() {
        return this.inventoryShow;
    }

    public boolean isInventoryShow() {
        return inventoryShow;
    }

    public void setInventoryShow(boolean inventoryShow) {
        this.inventoryShow = inventoryShow;
    }

    public int getWidthTile() {
        return width;
    }

    public void setWidthTile(int width) {
        this.width = width;
    }

    public int getHeightTile() {
        return height;
    }

    public void setHeightTile(int height) {
        this.height = height;
    }

    public void countItems() {
        for (Items item : this.countItems) {
            this.countItems.get(item.getNum()).setAmount(0);
        }
        for (Items item : itemsList) {
            countItems.get(item.getIdTile()).addAmount(item.getAmount());
        }
    }

    public void craftableItem() {
        this.craftableItemList = new ArrayList<>();
        this.craftableItemGraphicList = new ArrayList<>();
        int countItem = 51;
        Items itemsTmp;
        for (Items item : countItems) {
            if (item.getIdTile() == 15 && item.getAmount() >= 4) {
                itemsTmp = new Items(countItem, 2);
                craftableItemList.add(itemsTmp);
                craftableItemGraphicList.add(new ItemsGraphic(game, itemsTmp, this, dragAndDrop));
                countItem++;
            }
            if (item.getIdTile() == 3 && item.getAmount() >= 4) {
                itemsTmp = new Items(countItem, 3);
                craftableItemList.add(itemsTmp);
                craftableItemGraphicList.add(new ItemsGraphic(game, itemsTmp, this, dragAndDrop));
                countItem++;
            }

        }
    }

    public ArrayList<Items> getCraftableItemList() {
        return this.craftableItemList;
    }

    public float getScreenX() {
        return ScreenX;
    }

    public void setScreenX(float screenX) {
        ScreenX = screenX;
    }

    public float getScreenY() {
        return ScreenY;
    }

    public void setScreenY(float screenY) {
        ScreenY = screenY;
    }

    public ArrayList<ItemsGraphic> getCraftableItemGraphicList() {
        return craftableItemGraphicList;
    }

    public void setCraftableItemGraphicList(ArrayList<ItemsGraphic> craftableItemGraphicList) {
        this.craftableItemGraphicList = craftableItemGraphicList;
    }
}
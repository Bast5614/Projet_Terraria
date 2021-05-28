package terraria.game.actors.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import terraria.game.TerrariaGame;
import terraria.game.actors.Inventory.Inventory;
import terraria.game.actors.entities.Animation;
import terraria.game.actors.world.GeneratorMap.MapLoader;
import terraria.game.actors.world.GeneratorMap.DataMap;

import java.util.HashMap;


public class GameMap extends Actor {

    private HashMap<Integer, Texture> tilesTextures;
    private TextureRegion[][] plant;
    private TextureRegion [][] trees;
    private Texture sapling;
    private TextureRegion [][] pebble;
    private TextureRegion [][] filtre;

    TerrariaGame game;
    public Camera camera;
    private DataMap dataMap;
    private Animation breakingAnimation;

    boolean breakable;
    Vector3 breakingCoordinate = new Vector3();
    Inventory inventory;

    public int ScreenX, ScreenY,ScreenWidth,ScreenHeigth;


    public GameMap(TerrariaGame game) {
        this.game = game;
        breakable = false;

        //Create a new map or load it if it already exists
        dataMap = MapLoader.loadMap("test", "My Grass Lands!");


        //Load tile' textures
        tilesTextures = new HashMap<Integer, Texture>();
        for (TileType tile : TileType.values()) {
            FileHandle texture = Gdx.files.internal("tiles/"+tile.getName()+".png");
            if (texture.exists())
                tilesTextures.put(tile.getId(), game.getAssetManager().get("tiles/"+tile.getName()+".png", Texture.class));
        }

        //Load map elements' textures
        plant = TextureRegion.split( game.getAssetManager().get("herbes.png", Texture.class), TileType.TILE_SIZE, TileType.TILE_SIZE);
        trees =  TextureRegion.split(game.getAssetManager().get("arbres/arbreTest.png", Texture.class), 202, 375 );
        sapling =  game.getAssetManager().get("arbres/sapling.png", Texture.class);
        pebble =  TextureRegion.split(game.getAssetManager().get("cailloux.png", Texture.class), TileType.TILE_SIZE, TileType.TILE_SIZE);
        filtre = TextureRegion.split(game.getAssetManager().get("filtre.png", Texture.class), TileType.TILE_SIZE, TileType.TILE_SIZE);
        breakingAnimation =  new Animation(new TextureRegion(game.getAssetManager().get("breaking.png", Texture.class)),6 , 1);

    }


    public int[] getStartingPoint() {
        return dataMap.startingPoint;
    }
    public String getId() {
        return dataMap.id;
    }
    public int[][][] getMap() {
        return dataMap.map;
    }
    public String getName() {return dataMap.name;}
    public int getMapWidth() {
        return dataMap.width;
    }
    public int getMapHeight() {
        return dataMap.height;
    }
    public int getMapLayers() {
        return dataMap.map.length;
    }

    public void update(Camera camera, Stage stage){
        this.camera = camera;
        Vector3 vec = camera.position;
        ScreenX =  (int)vec.x -  stage.getViewport().getScreenWidth()/2;;
        ScreenY = (int)vec.y -  stage.getViewport().getScreenHeight()/2;
        ScreenWidth =   stage.getViewport().getScreenWidth();
        ScreenHeigth = stage.getViewport().getScreenHeight();

    }


    /**
     * Donne les coordonnées (dans la matrice) de la tuile à cette position de souris
     * @param layer
     * @param x
     * @param y
     * @return
     */
    public Vector3 getTileCoordinateByLocation(int layer, float x, float y) {
        return new Vector3((int) (x / TileType.TILE_SIZE), (int) (getMapHeight() - (y / TileType.TILE_SIZE)), layer);
    }

    public boolean presentTile(Vector3 coordinate) {
        if (tileInMap(coordinate))  {
            if (getMap()[(int)coordinate.z][(int)coordinate.y][(int)coordinate.x] != 0)
                return true;
        }
        return false;
    }

    public boolean tileInMap(Vector3 coordinate) {
        if (coordinate.x > 0 && coordinate.x < getMapWidth() && coordinate.y > 0 && coordinate.y < getMapHeight())  {
            return true;
        }
        return false;
    }
    public void initDestroyTile(Vector3 coordinate, Inventory inventory){
        if (presentTile(coordinate)) {
            breakable = true;
            breakingCoordinate = coordinate;

            this.inventory = inventory;

            if (getMap()[(int) coordinate.z][(int) coordinate.y][(int) coordinate.x] == TileType.LAVA.getId()) {
                //bloc indestructible
                breakable = false;
            } else {
                breakable = true;
            }
        }
    }
    public void drawBreaking(Batch batch){
        if(breakable){

            breakingAnimation.update(Gdx.graphics.getDeltaTime());
            batch.draw(breakingAnimation.getFrame(), breakingCoordinate.x * TileType.TILE_SIZE, getPixelHeight() - (breakingCoordinate.y +1) * TileType.TILE_SIZE );
            if(breakingAnimation.frame > 4){ breakable = false; destroyTile(breakingCoordinate, inventory); breakingAnimation.frame = 0;}

            if( !Gdx.input.isButtonPressed(Input.Buttons.LEFT) ||  Gdx.input.getDeltaX() != 0 || Gdx.input.getDeltaY() != 0){
                breakable = false; breakingAnimation.frame = 0;

            }
        }
    }

    public void destroyTile(Vector3 coordinate, Inventory inventory){
        int  treeSize = 0;
        int idBloc = getMap()[(int)coordinate.z][(int)coordinate.y][(int)coordinate.x];
        int idBlocSupp = getMap()[(int)coordinate.z][(int)coordinate.y-1][(int)coordinate.x];

        if (idBloc == TileType.LOG.getId() || idBlocSupp == TileType.LOG.getId() ) {
            //on détruit tout l'arbre et on note sa taille
            treeSize = 1;
            int y = (int) coordinate.y - 1;
            while (getMap()[(int) coordinate.z][y][(int) coordinate.x] == TileType.LOG.getId()) {
                getMap()[(int) coordinate.z][y][(int) coordinate.x] = 0;
                y--;
                treeSize++;
            }
            y = (int) coordinate.y + 1;
            while (getMap()[(int) coordinate.z][y][(int) coordinate.x] == TileType.LOG.getId()) {
                getMap()[(int) coordinate.z][y][(int) coordinate.x] = 0;
                y++;
                treeSize++;
            }

        }
        if (idBlocSupp == TileType.WEED.getId() || idBlocSupp == TileType.PEBBLE.getId() || idBlocSupp == TileType.SAPLING.getId()) {
            //On récupère les éléments posés sur le bloc
            getMap()[(int)coordinate.z][(int)coordinate.y-1][(int)coordinate.x] = 0;
            inventory.addTileInInventory(idBlocSupp);
        }


        if (treeSize > 0) { //Une fonction pour rajouter plusieurs items dans l'inventaire serait cool
            inventory.addTileInInventory(TileType.SAPLING.getId());
            inventory.addTileInInventory(TileType.SAPLING.getId());
            for (int i = 0; i < treeSize*2; i++)
                inventory.addTileInInventory(TileType.PLANKS.getId());
        } else
            inventory.addTileInInventory(idBloc);

        getMap()[(int)coordinate.z][(int)coordinate.y][(int)coordinate.x] = 0;


    }

    public void addTile(Vector3 coordinate, Inventory inventory) {
        if (tileInMap(coordinate) && !presentTile(coordinate)) {

            int type = inventory.getItemsList().get(inventory.getCurrentItems()).getIdTile();
            if (type == TileType.WEED.getId() || type == TileType.SAPLING.getId()) {
                if (getMap()[(int)coordinate.z][(int)coordinate.y+1][(int)coordinate.x] == TileType.GRASS.getId()) {
                    getMap()[(int)coordinate.z][(int)coordinate.y][(int)coordinate.x] = type;
                    inventory.getItemsList().get(inventory.getCurrentItems()).decrAmount();
                }
            } else {
                getMap()[(int)coordinate.z][(int)coordinate.y][(int)coordinate.x] = type;
                inventory.getItemsList().get(inventory.getCurrentItems()).decrAmount();
            }

        }
    }

    /**
     * vrais si la case de coordonnée x,y est un obstacle, faut sinon
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public boolean doesRectCollideWithMap(float x, float y, int width, int height){
        if (x < 0 || y < 0 || x + width > getPixelWidth() || y + height > getPixelHeight()){
            return true;
        }
        for (int row = (int) (y / TileType.TILE_SIZE); row < Math.ceil((y + height ) / TileType.TILE_SIZE); row++) {
            for (int col = (int) (x / TileType.TILE_SIZE); col < Math.ceil((x +width ) / TileType.TILE_SIZE); col++) {
                for (int layer = 0; layer < getMapLayers(); layer++) {
                    TileType type = getTileTypeByCoordinate(layer, col, row);
                    if (type != null && type.isCollidable()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * retourne le type d'une case en fonction de sa localisation sur l'ecran (en pixel)
     * @param layer
     * @param x
     * @param y
     * @return
     */
    public TileType getTileTypeByLocation(int layer, float x, float y) {
        return this.getTileTypeByCoordinate(layer, (int) (x / TileType.TILE_SIZE), (int) (y / TileType.TILE_SIZE));
    }

    /**
     * retourne le type d'une case en fonction de ses coordonnées en jeu
     * @param layer
     * @param col
     * @param row
     * @return
     */
    public TileType getTileTypeByCoordinate(int layer, int col, int row) {
        if (col < 0 || col >= getMapWidth() || row < 0 || row >= getMapHeight())
            return null;

        int id = getMap()[layer][(getMapHeight() - row - 1)][col];
        if(id == 0){return null;}
        return TileType.getTileTypeById(id);
    }

    public int getPixelWidth(){return this.getMapWidth() * TileType.TILE_SIZE;}
    public int getPixelHeight(){return this.getMapHeight() * TileType.TILE_SIZE; }

    public void applyFilter(Batch batch, int col, int row, int numFiltre){
        int srcFunc = batch.getBlendSrcFunc();
        int dstFunc = batch.getBlendDstFunc();
        batch.enableBlending();
        batch.setBlendFunction(Gdx.gl20.GL_ZERO, Gdx.gl20.GL_SRC_COLOR);
        batch.draw(filtre[0][numFiltre], col * TileType.TILE_SIZE, row * TileType.TILE_SIZE);
        batch.setBlendFunction(srcFunc, dstFunc);
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {


        for (int layer = 0; layer < getMap().length; layer++) {
            for (int row = 0; row < getMap()[0].length; row++) {
                for (int col = 0; col < getMap()[0][0].length; col++) {

                    if( col* TileType.TILE_SIZE - 128 <= ScreenX + ScreenWidth && (col * TileType.TILE_SIZE + 128>= ScreenX) && (row * TileType.TILE_SIZE - 128 <= ScreenY + ScreenHeigth && row * TileType.TILE_SIZE + 128 >= ScreenY)) {


                        TileType type = getTileTypeByCoordinate(layer, col, row);


                            if (type != null) {

                                switch (type) {

                                    case LOG:
                                        //applyFilter(batch,col,  row, 4);
                                        if (getTileTypeByCoordinate(layer, col, row - 1) == TileType.GRASS)
                                            batch.draw(trees[0][col % 13], col * TileType.TILE_SIZE - 85, row * TileType.TILE_SIZE);
                                        break;
                                    case PEBBLE:
                                        batch.draw(pebble[0][col % 8], col * TileType.TILE_SIZE, row * TileType.TILE_SIZE);
                                        break;
                                    case WEED:
                                        batch.draw(plant[0][col % 4], col * TileType.TILE_SIZE, row * TileType.TILE_SIZE);
                                        break;
                                    case SAPLING:
                                        batch.draw(sapling, col * TileType.TILE_SIZE, row * TileType.TILE_SIZE);
                                        break;


                                    case LIGHTSOURCE0: break;
                                    case LIGHTSOURCE1: applyFilter(batch,col,  row, 1); break;
                                    case LIGHTSOURCE2: applyFilter(batch,col,  row, 2);break;
                                    case LIGHTSOURCE3: applyFilter(batch,col,  row, 3); break;
                                    case LIGHTSOURCE4: applyFilter(batch,col,  row, 4); break;
                                    case LIGHTSOURCE5: applyFilter(batch,col,  row, 5); break;
                                    case LIGHTSOURCE6: applyFilter(batch,col,  row, 6); break;
                                    case LIGHTSOURCE7: applyFilter(batch,col,  row, 7); break;
                                    case NOLIGHT: applyFilter(batch,col,  row, 8); break;


                                    default:
                                        batch.draw(tilesTextures.get(type.getId()), col * TileType.TILE_SIZE, row * TileType.TILE_SIZE);
                                        break;
                            }


                        }
                    }
                }
            }
        }
        drawBreaking(batch);
    }


}


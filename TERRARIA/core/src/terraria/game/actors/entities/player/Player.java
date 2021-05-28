package terraria.game.actors.entities.player;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import terraria.game.TerrariaGame;
import terraria.game.actors.Inventory.Inventory;
import terraria.game.actors.entities.*;
import terraria.game.actors.world.CalculatorLight;
import terraria.game.actors.world.GameMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import terraria.game.actors.world.TileType;
import terraria.game.screens.LoadingScreen;
import java.awt.MouseInfo;

public class Player extends Entity {

    private static final int SPEED = 200, JUMP_VELOCITY = 5;

    private static final double FALLDAMAGE_COEFF = -0.005;
    private static float fallDamage;

    private PlayerHealth playerHealth;
    private Inventory inventory;
    private boolean invulnerable;
    private final float INVULNERABILITY_TIME = 1f;  //1f = 1sec
    private float invulnerabilityTimer = 0f;
    public static int MAXHEALTH = 10;
    private Vector3 worldCoordinates = new Vector3(0,0,0);
    Boolean tooHigh = false;


    private static int state = 0;
    private static final int IDLE = 0, JUMPING = 1, RUNNING = 2, HIT = 3;

    @Override
    public void create(EntitySnapshot snapshot, EntityType type, GameMap gameMap, TerrariaGame game) {
        super.create(snapshot, type, gameMap,game);
        TextureRegion[][] heart =  TextureRegion.split(game.getAssetManager().get("heart.png", Texture.class), 35, 35);
        this.playerHealth = new PlayerHealth(game, heart, snapshot.health);
        this.inventory = new Inventory(game);
        this.inventory.fillInventory(snapshot.inventory);
        init();

    }
    public void create(int posX, int posY, EntityType type, GameMap gameMap, TerrariaGame game) {
        super.create(posX,posY, type, gameMap,game);
        TextureRegion[][] heart =  TextureRegion.split(game.getAssetManager().get("heart.png", Texture.class), 35, 35);
        this.playerHealth = new PlayerHealth(game, heart, MAXHEALTH);
        this.inventory = new Inventory(game);
        init();
    }


    public void init(){

        invulnerable = false;
        animations = new Array<>();
        for(int i = 0; i < LoadingScreen.TEXTURE_NUMBER_PLAYER ; i++){
            switch (i){
                case 0: animations.add(new Animation(new TextureRegion(game.getAssetManager().get("playerAnimation/player"+i+".png", Texture.class)),2 , 0.5F));break;
                case 1: animations.add(new Animation(new TextureRegion(game.getAssetManager().get("playerAnimation/player"+i+".png", Texture.class)),1 , 0.5F));break;
                case 2: animations.add(new Animation(new TextureRegion(game.getAssetManager().get("playerAnimation/player"+i+".png", Texture.class)),6 , 0.5F));break;
                case 3: animations.add(new Animation(new TextureRegion(game.getAssetManager().get("playerAnimation/player"+i+".png", Texture.class)),2 , 0.1F));break;
            }
        }
    }


    /**
     * Controle du joueur
     * @param deltaTime
     * @param gravity
     */
    @Override
    public void update(float deltaTime, float gravity, Camera camera, Stage stage) {
        //Handle the camera

        if (Gdx.input.getX() <= 25) {
            if (pos.x - camera.position.x  < 100) {
                camera.position.set(camera.position.x - SPEED * deltaTime, pos.y + 32 * 5, camera.position.z);
            }
            if (pos.x - camera.position.x > 200) {
                camera.position.set(pos.x - 200, pos.y+ 32*5, camera.position.z);
            }
        } else if (Gdx.input.getX() > gameMap.ScreenWidth - 25) {
            if (camera.position.x - pos.x  < 100) {
                camera.position.set(camera.position.x + SPEED * deltaTime, pos.y+ 32*5, camera.position.z);
            }
            if (camera.position.x - pos.x > 200) {
                camera.position.set(pos.x + 200, pos.y+ 32*5, camera.position.z);
            }
        } else {
            if (camera.position.x < pos.x - SPEED * deltaTime)  {
                camera.position.set(camera.position.x + SPEED * deltaTime, pos.y+ 32*5, camera.position.z);
            } else if (camera.position.x > pos.x + SPEED * deltaTime){
                camera.position.set(camera.position.x - SPEED * deltaTime, pos.y + 32*5, 0);
            } else {
                camera.position.set(pos.x , pos.y + 32*5, 0);
            }
            if (camera.position.x - pos.x > 250 || pos.x - camera.position.x > 250){
                camera.position.set(pos.x , pos.y + 32*5, 0);
            }
        }

        camera.unproject(worldCoordinates);


        //Handle the jump
        if ((Gdx.input.isKeyPressed(Keys.SPACE) || Gdx.input.isKeyPressed(Keys.Z)) && grounded) {
            this.velocityY += JUMP_VELOCITY * getWeight();
        }
        else if (Gdx.input.isKeyPressed(Keys.SPACE) && !grounded && this.velocityY > 0) {
            this.velocityY += JUMP_VELOCITY * getWeight() * deltaTime;
        }

        //Check if falling from too high
        fallingCheck();

        super.update(deltaTime, gravity, camera, stage);   //Apply gravity

        //Handle the controls
        if (Gdx.input.isKeyPressed(Keys.Q)) {
            moveX(-SPEED * deltaTime);
        }
        else if (Gdx.input.isKeyPressed(Keys.D)) {
            moveX(SPEED * deltaTime);
        }



        //Check the invulnerability frame
        if (invulnerable) {
            invulnerabilityTimer += deltaTime;
            if (invulnerabilityTimer > INVULNERABILITY_TIME) {
                invulnerabilityTimer = 0f;
                invulnerable = false;
            }
        }

        //Check the state of the character
        if (invulnerable) state = HIT;
        else if (!grounded) state = JUMPING;
        else if (Gdx.input.isKeyPressed(Keys.Q) || Gdx.input.isKeyPressed(Keys.D)) state = RUNNING;
        else state = IDLE;

        //Update health
        playerHealth.update(camera,stage);

        //Update light
        CalculatorLight.calculatorLightSource( (int)((pos.x +getWidth()/2)/ TileType.TILE_SIZE ), (int)(gameMap.getMapHeight() - ((pos.y + getHeight()/2)/ TileType.TILE_SIZE)), gameMap.getMap());

    }

    public void takeAhit(double damage) {
        if (!invulnerable) {
            playerHealth.ApplyDamage(damage);
            if (grounded)
                this.velocityY += 2 * getWeight();  //Knockback
            invulnerable = true;
        }
    }

    public void fallingCheck() {
        if (velocityY < -350) {
            tooHigh = true;
            fallDamage = velocityY;
        }
        if (isGrounded() && tooHigh) {
            takeAhit(fallDamage * FALLDAMAGE_COEFF);
            tooHigh = false;
        }
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public EntitySnapshot getSaveSnapshot() {
        EntitySnapshot snapshot = super.getSaveSnapshot();
        snapshot.health = playerHealth.health;
        snapshot.inventory = inventory.getItemsList();
        return snapshot;
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        TextureRegion texture;

        switch (state) {
            case JUMPING:
                texture = animations.get(1).getFrame();
                break;
            case RUNNING:
                texture = animations.get(2).getFrame();
                animations.get(2).update( Gdx.graphics.getDeltaTime());
                break;
            case HIT:
                texture = animations.get(3).getFrame();
                animations.get(3).update( Gdx.graphics.getDeltaTime());
                break;
            default :   //state == IDLE
                texture = animations.get(0).getFrame();
                animations.get(0).update( Gdx.graphics.getDeltaTime());
                break;
        }

        batch.draw(texture, flipX ? pos.x+getWidth() : pos.x, pos.y, flipX ? -getWidth() : getWidth(), getHeight());
        playerHealth.draw(batch,parentAlpha);
    }
}

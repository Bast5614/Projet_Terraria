package terraria.game.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import terraria.game.actors.world.ParallaxBackground;

public class MainMenuScreen extends ScreenAdapter {

    private Stage stage;
    private Camera camera;
    private ImageButton playButton;
    private ImageButton exitButton;
    private final Game game;
    private ParallaxBackground parallaxBackground;
    private Array<Texture> MainScreenParallax;

    TextureRegion play;

    public MainMenuScreen(Game game){
        this.game = game;

        //Initialize the stage and camera
        stage = new Stage(new ScreenViewport());
        camera = (OrthographicCamera) stage.getViewport().getCamera();

        //Initialize menu's buttons
        play = new TextureRegion(new Texture(Gdx.files.internal("background/play.png")));
        TextureRegion playPressed = new TextureRegion(new Texture(Gdx.files.internal("background/playPressed.png")));

        playButton = new ImageButton( new TextureRegionDrawable(play), new TextureRegionDrawable(playPressed));
        playButton.setPosition(stage.getViewport().getScreenWidth()/2,stage.getViewport().getScreenHeight()/2, Align.center);
        playButton.addListener(new ActorGestureListener() {

            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                super.tap(event, x, y, count, button);
                startGame();
                dispose();

            }
        });

        TextureRegion exit = new TextureRegion(new Texture(Gdx.files.internal("background/exit.png")));
        TextureRegion exitPressed = new TextureRegion(new Texture(Gdx.files.internal("background/exitPressed.png")));

        exitButton = new ImageButton( new TextureRegionDrawable(exit), new TextureRegionDrawable(exitPressed));
        exitButton.setPosition(stage.getViewport().getScreenWidth()/2,(stage.getViewport().getScreenHeight()/2)-play.getRegionHeight(), Align.center);
        exitButton.addListener(new ActorGestureListener() {

            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                super.tap(event, x, y, count, button);
                dispose();
                Gdx.app.exit();
            }
        });

        //Set the menu's background
        MainScreenParallax = new Array<Texture>();
        for(int i = 1; i < 3;i++){
            MainScreenParallax.add(new Texture(Gdx.files.internal("background/img"+i+".png")));
            MainScreenParallax.get(MainScreenParallax.size-1).setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);
        }

        this.parallaxBackground = new ParallaxBackground(MainScreenParallax, true);
        parallaxBackground.setSize(stage.getViewport().getScreenWidth(),stage.getViewport().getScreenHeight());
        parallaxBackground.setSpeed(1);

        //Set the stage
        stage.addActor(parallaxBackground);
        stage.addActor(playButton);
        stage.addActor(exitButton);
    }

    public void startGame(){
        game.setScreen(new GameScreen());
    }

    /**
     * Called when the screen should render itself.
     * @param delta
     */
    @Override
    public void	render(float delta){
        playButton.setPosition(stage.getViewport().getScreenWidth()/2,stage.getViewport().getScreenHeight()/2, Align.center);
        exitButton.setPosition(stage.getViewport().getScreenWidth()/2,stage.getViewport().getScreenHeight()/2-play.getRegionHeight()-10, Align.center);
        stage.act(delta);
        stage.draw();

        this.parallaxBackground.update((OrthographicCamera) camera, stage);
    }

    /**
     * Called when this screen should release all resources.
     */
    @Override
    public void dispose(){
        stage.dispose();

    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    @Override
    public void hide(){}


    @Override
    public void	pause(){}


    @Override
    public void	resize(int width, int height){
        stage.getViewport().update(width,height,true );
        parallaxBackground.setSize(stage.getViewport().getWorldWidth(),stage.getViewport().getWorldHeight());
    }


    @Override
    public void	resume(){}

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    @Override
    public void	show(){ Gdx.input.setInputProcessor(stage);}



}

package rs;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;

class RectangleSelectionTest extends SimpleApplication {
    private Spatial model;

    public static void main(String[] args) {
        RectangleSelectionTest game = new RectangleSelectionTest();
        AppSettings appSettings = new AppSettings(true);
        appSettings.setUseJoysticks(true);
        appSettings.setGammaCorrection(true);
        appSettings.setSamples(16);
        appSettings.setFrameRate(-1);
        appSettings.setResizable(true);
        game.setSettings(appSettings);
        game.setShowSettings(false);
        game.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);

        DirectionalLight directionalLight = new DirectionalLight(
                new Vector3f(-1, -1, -1).normalizeLocal(),
                ColorRGBA.White.clone()
        );

        rootNode.addLight(directionalLight);

        model = assetManager.loadModel("rs/DamagedHelmet.gltf");
        model.setLocalScale(3);

        rootNode.attachChild(model);

        viewPort.setBackgroundColor(ColorRGBA.Black);

        stateManager.attach(RectangleSelection.getInstance());
    }

    @Override
    public void simpleUpdate(float tpf) {
        model.rotate(tpf * .2f, tpf * .3f, tpf * .4f);
    }
}
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;

import map.Shop;
import entities.Player;
import entities.Police;

import systems.GameState;
import systems.MapType;
import systems.SoundManager;

public class Game extends JPanel implements KeyListener {

    // =====================================================
    // MAP IMAGES
    // =====================================================

    private BufferedImage mainMapImage;
    private BufferedImage houseMapImage;
    private BufferedImage storeMapImage;

    // =====================================================
    // SPRITE / HITBOX SIZE
    // =====================================================

    // ขนาด Hitbox ของตัวละครยังเหมือนเดิม
    private final int PLAYER_HITBOX_SIZE = 40;
    private final int POLICE_HITBOX_SIZE = 40;

    /*
     * ขนาดสูงสุดของ Sprite ที่แสดง
     *
     * สำคัญ:
     * ไม่บังคับ Sprite เป็น 32x32 แล้ว
     * แต่จะรักษาสัดส่วนรูปต้นฉบับ
     */
    private final int PLAYER_SPRITE_WIDTH = 32;
    private final int PLAYER_SPRITE_HEIGHT = 40;

    private final int POLICE_SPRITE_WIDTH = 32;
    private final int POLICE_SPRITE_HEIGHT = 40;

    // =====================================================
    // ANIMATION
    // =====================================================

    private final int ANIMATION_DELAY = 120;

    private long playerAnimationTimer = 0;
    private long rikAnimationTimer = 0;
    private long gaffyAnimationTimer = 0;

    private int playerFrame = 0;
    private int rikFrame = 0;
    private int gaffyFrame = 0;

    // A = ซ้าย
    // D = ขวา
    // W = ขึ้น
    // S = ลง

    private String playerDirection = "S";
    private String rikDirection = "S";
    private String gaffyDirection = "S";

    // เวลาที่ animation โจมตี / โต้ตอบจะจบ
    private long rikAttackEndTime = 0;
    private long gaffyAttackEndTime = 0;
    private long interactAnimEndTime = 0;

    // =====================================================
    // ON-SCREEN MESSAGE
    // =====================================================

    private String message = "";
    private long messageEndTime = 0;

    // =====================================================
    // SPRITE ARRAYS
    // =====================================================

    private List<BufferedImage> playerStand = new ArrayList<>();
    private List<BufferedImage> playerMoveA = new ArrayList<>();
    private List<BufferedImage> playerMoveD = new ArrayList<>();
    private List<BufferedImage> playerMoveS = new ArrayList<>();
    private List<BufferedImage> playerMoveW = new ArrayList<>();
    private List<BufferedImage> playerDelivery = new ArrayList<>();
    private List<BufferedImage> playerGameOver = new ArrayList<>();

    private List<BufferedImage> RMoveA = new ArrayList<>();
    private List<BufferedImage> RMoveD = new ArrayList<>();
    private List<BufferedImage> RMoveS = new ArrayList<>();
    private List<BufferedImage> RMoveW = new ArrayList<>();

    private List<BufferedImage> RRunA = new ArrayList<>();
    private List<BufferedImage> RRunD = new ArrayList<>();
    private List<BufferedImage> RRunS = new ArrayList<>();
    private List<BufferedImage> RRunW = new ArrayList<>();

    private List<BufferedImage> RAttack = new ArrayList<>();

    private List<BufferedImage> gaffyMoveA = new ArrayList<>();
    private List<BufferedImage> gaffyMoveD = new ArrayList<>();
    private List<BufferedImage> gaffyMoveS = new ArrayList<>();
    private List<BufferedImage> gaffyMoveW = new ArrayList<>();

    private List<BufferedImage> gaffyRunA = new ArrayList<>();
    private List<BufferedImage> gaffyRunD = new ArrayList<>();
    private List<BufferedImage> gaffyRunS = new ArrayList<>();
    private List<BufferedImage> gaffyRunW = new ArrayList<>();

    private List<BufferedImage> gaffyAttack = new ArrayList<>();

    // แม่ (จุดจ่ายเงินในบ้าน) และ NPC (จุดส่งสินค้าในแมพหลัก)
    private List<BufferedImage> momStand = new ArrayList<>();
    private List<BufferedImage> npcStand = new ArrayList<>();

    // รูปนิ่งสำรอง ใช้เมื่อโหลด animation ไม่ได้
    private BufferedImage momImage;
    private BufferedImage npcImage;

    // =====================================================
    // ITEM DATA (ลำดับ = ปุ่ม 1-4)
    // =====================================================

    private final String[] ITEM_NAMES = {
        "Time Stop Watch",
        "Wing Shoes",
        "Powder Ticket",
        "Vaccine"
    };

    private final int[] ITEM_PRICES = {
        300,
        300,
        400,
        200
    };

    private final String[] ITEM_DESC = {
        "Stops the police for 10 sec",
        "Speed +50% for 10 sec",
        "Money +20% for 30 sec",
        "Heals 10 HP"
    };

    private final BufferedImage[] itemImages =
        new BufferedImage[4];

    // หน้าเริ่มเกม / หน้า Game Over
    private BufferedImage startPageImage;
    private BufferedImage gameOverImage;

    // เวลาที่เกมจบ (ใช้โชว์เวลาที่รอด)
    private long gameEndTime = 0;

    // =====================================================
    // SOUND
    // =====================================================

    private final SoundManager sound = new SoundManager();

    private final float BGM_VOLUME = 0.45f;
    private final float SFX_VOLUME = 0.9f;

    private int getItemCount(int index) {

        switch (index) {

            case 0:
                return player.getWatchCount();

            case 1:
                return player.getShoesCount();

            case 2:
                return player.getTicketCount();

            default:
                return player.getVaccineCount();
        }
    }

    // =====================================================
    // LOAD IMAGE
    // =====================================================

    private BufferedImage loadImage(String path) {

        try {

            return ImageIO.read(new File(path));

        } catch (IOException e) {

            System.out.println("Failed to load image: " + path);

            return null;
        }
    }

    // =====================================================
    // LOAD MAP IMAGES
    // =====================================================

    private void loadMapImages() {

        mainMapImage = loadImage("asset/map/MainMap.jpg");

        houseMapImage = loadImage("asset/map/House.png");

        storeMapImage = loadImage("asset/map/Store.png");
    }

    // =====================================================
    // LOAD ALL SPRITES
    // =====================================================

    private void loadSprites() {

        System.out.println("Loading character sprites...");

        // =================================================
        // YUSUP
        // =================================================

        playerStand =
            loadAnimation(
                "asset/characters/Yusup/Stand",
                "Stand"
            );

        playerMoveA =
            loadAnimation(
                "asset/characters/Yusup/MoveA",
                "MoveA"
            );

        playerMoveD =
            loadAnimation(
                "asset/characters/Yusup/MoveD",
                "MoveD"
            );

        playerMoveS =
            loadAnimation(
                "asset/characters/Yusup/MoveS",
                "MoveS"
            );

        playerMoveW =
            loadAnimation(
                "asset/characters/Yusup/MoveW",
                "MoveW"
            );

        playerDelivery =
            loadAnimation(
                "asset/characters/Yusup/Delivery",
                "Delivery"
            );

        playerGameOver =
            loadAnimation(
                "asset/characters/Yusup/GameOver",
                "GameOver"
            );

        // =================================================
        // RIK
        // =================================================

        RMoveA =
            loadAnimation(
                "asset/characters/Rik/RMoveA",
                "RMoveA"
            );

        RMoveD =
            loadAnimation(
                "asset/characters/Rik/RMoveD",
                "RMoveD"
            );

        RMoveS =
            loadAnimation(
                "asset/characters/Rik/RMoveS",
                "RMoveS"
            );

        RMoveW =
            loadAnimation(
                "asset/characters/Rik/RMoveW",
                "RMoveW"
            );

        RRunA =
            loadAnimation(
                "asset/characters/Rik/RRunA",
                "RRunA"
            );

        RRunD =
            loadAnimation(
                "asset/characters/Rik/RRunD",
                "RRunD"
            );

        RRunS =
            loadAnimation(
                "asset/characters/Rik/RRunS",
                "RRunS"
            );

        RRunW =
            loadAnimation(
                "asset/characters/Rik/RRunW",
                "RRunW"
            );

        RAttack =
            loadAnimation(
                "asset/characters/Rik/RAttack",
                "RAttack"
            );

        // =================================================
        // GAFFY
        // =================================================

        gaffyMoveA =
            loadAnimation(
                "asset/characters/Guffy/GMoveA",
                "GMoveA"
            );

        gaffyMoveD =
            loadAnimation(
                "asset/characters/Guffy/GMoveD",
                "GMoveD"
            );

        gaffyMoveS =
            loadAnimation(
                "asset/characters/Guffy/GMoveS",
                "GMoveS"
            );

        gaffyMoveW =
            loadAnimation(
                "asset/characters/Guffy/GMoveW",
                "GMoveW"
            );

        gaffyRunA =
            loadAnimation(
                "asset/characters/Guffy/GRunA",
                "GRunA"
            );

        gaffyRunD =
            loadAnimation(
                "asset/characters/Guffy/GRunD",
                "GRunD"
            );

        gaffyRunS =
            loadAnimation(
                "asset/characters/Guffy/GRunS",
                "GRunS"
            );

        gaffyRunW =
            loadAnimation(
                "asset/characters/Guffy/GRunW",
                "GRunW"
            );

        gaffyAttack =
            loadAnimation(
                "asset/characters/Guffy/GAttack",
                "GAttack"
            );

        // =================================================
        // MOM / NPC
        // =================================================

        momStand =
            loadAnimation(
                "asset/characters/Mom/MStand",
                "MStand"
            );

        npcStand =
            loadAnimation(
                "asset/characters/NPC/NStand",
                "NStand"
            );

        itemImages[0] = loadImage("asset/items/Watch.png");
        itemImages[1] = loadImage("asset/items/Shoes.png");
        itemImages[2] = loadImage("asset/items/Ticket.png");
        itemImages[3] = loadImage("asset/items/Vaccine.png");

        startPageImage = loadImage("asset/Status/Startpage.jpg");
        gameOverImage = loadImage("asset/Status/Gameover.jpg");

        momImage =
            loadImage("asset/characters/Mom/Mom.png");

        npcImage =
            loadImage("asset/characters/NPC/NPC.png");

        System.out.println("Character sprites loaded!");
    }

    // =====================================================
    // LOAD ANIMATION
    // =====================================================

    private List<BufferedImage> loadAnimation(
        String folderPath,
        String prefix
    ) {

        List<BufferedImage> frames = new ArrayList<>();

        File folder = new File(folderPath);

        if (!folder.exists()) {

            System.out.println(
                "Folder not found: " + folderPath
            );

            return frames;
        }

        File[] files = folder.listFiles();

        if (files == null) {

            return frames;
        }

        List<File> imageFiles = new ArrayList<>();

        for (File file : files) {

            if (!file.isFile()) {

                continue;
            }

            String name = file.getName();

            if (
                name.toLowerCase().endsWith(".png")
                &&
                name.startsWith(prefix)
            ) {

                imageFiles.add(file);
            }
        }

        // =================================================
        // SORT FRAME
        // =================================================

        Collections.sort(
            imageFiles,
            (a, b) ->
                Integer.compare(
                    getFrameNumber(a.getName()),
                    getFrameNumber(b.getName())
                )
        );

        // =================================================
        // LOAD FRAME
        // =================================================

        for (File file : imageFiles) {

            try {

                BufferedImage image =
                    ImageIO.read(file);

                if (image != null) {

                    frames.add(image);
                }

            } catch (IOException e) {

                System.out.println(
                    "Cannot load: " + file.getPath()
                );
            }
        }

        System.out.println(
            prefix + " = " + frames.size() + " frames"
        );

        return frames;
    }

    // =====================================================
    // GET FRAME NUMBER
    // =====================================================

    private int getFrameNumber(String fileName) {

        String number = "";

        for (
            int i = fileName.length() - 1;
            i >= 0;
            i--
        ) {

            char c = fileName.charAt(i);

            if (Character.isDigit(c)) {

                number = c + number;

            } else if (!number.isEmpty()) {

                break;
            }
        }

        if (number.isEmpty()) {

            return 0;
        }

        return Integer.parseInt(number);
    }

    // =====================================================
    // GET CURRENT FRAME
    // =====================================================

    private BufferedImage getFrame(
        List<BufferedImage> animation,
        int frame
    ) {

        if (
            animation == null
            ||
            animation.isEmpty()
        ) {

            return null;
        }

        return animation.get(
            frame % animation.size()
        );
    }

    // =====================================================
    // DRAW CHARACTER WITH CORRECT ASPECT RATIO
    // =====================================================

    /*
     * ฟังก์ชันนี้สำคัญที่สุดสำหรับแก้ปัญหา
     * ตัวละครดูบาน / อ้วน / แบน
     *
     * จะไม่ยืดรูปให้เป็นขนาดที่กำหนดตรงๆ
     * แต่คำนวณจากขนาดรูปต้นฉบับ
     */
    private void drawCharacterSprite(
        Graphics g,
        BufferedImage image,
        int hitboxX,
        int hitboxY,
        int maxWidth,
        int maxHeight
    ) {

        drawCharacterSprite(
            g,
            image,
            hitboxX,
            hitboxY,
            maxWidth,
            maxHeight,
            PLAYER_HITBOX_SIZE
        );
    }

    private void drawCharacterSprite(
        Graphics g,
        BufferedImage image,
        int hitboxX,
        int hitboxY,
        int maxWidth,
        int maxHeight,
        int hitboxSize
    ) {

        if (image == null) {

            return;
        }

        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        if (
            originalWidth <= 0
            ||
            originalHeight <= 0
        ) {

            return;
        }

        // =================================================
        // คำนวณ Scale โดยรักษา Aspect Ratio
        // =================================================

        double scaleX =
            (double) maxWidth / originalWidth;

        double scaleY =
            (double) maxHeight / originalHeight;

        double scale =
            Math.min(scaleX, scaleY);

        int drawWidth =
            Math.max(
                1,
                (int) Math.round(
                    originalWidth * scale
                )
            );

        int drawHeight =
            Math.max(
                1,
                (int) Math.round(
                    originalHeight * scale
                )
            );

        // =================================================
        // จัด Sprite ให้อยู่กลาง Hitbox
        // =================================================

        int drawX =
            hitboxX
            +
            (hitboxSize - drawWidth) / 2;

        int drawY =
            hitboxY
            +
            (hitboxSize - drawHeight) / 2;

        g.drawImage(
            image,
            drawX,
            drawY,
            drawWidth,
            drawHeight,
            null
        );
    }

    // =====================================================
    // SHOP
    // =====================================================

    private boolean shopOpen = false;

    private Shop shop;

    // =====================================================
    // PLAYER / POLICE
    // =====================================================

    private Player player;

    private Police rik;

    private Police gaffy;

    // =====================================================
    // KEYBOARD
    // =====================================================

    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;

    private boolean spacePressed;

    private final Set<Integer> heldKeys =
        new HashSet<>();

    // =====================================================
    // GAME LOOP / STATE
    // =====================================================

    private Timer timer;

    private GameState gameState;

    private MapType currentMap;

    // =====================================================
    // SCORE
    // =====================================================

    private long startTime;

    private long lastScoreTime;

    // =====================================================
    // MOTHER PAYMENT
    // =====================================================

    private long lastMotherPaymentTime;

    private int motherPayment = 200;

    private boolean motherPaymentReady = false;

    // =====================================================
    // BUTTON
    // =====================================================

    private JButton startButton;

    private JButton restartButton;

    private JButton exitButton;

    // =====================================================
    // MAP SIZE
    // =====================================================

    private final int MAP_WIDTH = 1910;

    private final int MAP_HEIGHT = 1000;

    // =====================================================
    // POLICE COLLISION WALLS
    // =====================================================

    private Rectangle[] policeWalls = {

        new Rectangle(
            0,
            0,
            MAP_WIDTH,
            40
        ),

        new Rectangle(
            0,
            MAP_HEIGHT - 40,
            MAP_WIDTH,
            40
        ),

        new Rectangle(
            0,
            0,
            40,
            MAP_HEIGHT
        ),

        new Rectangle(
            MAP_WIDTH - 40,
            0,
            40,
            MAP_HEIGHT
        )
    };

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public Game() {

        setPreferredSize(
            new Dimension(
                MAP_WIDTH,
                MAP_HEIGHT
            )
        );

        setBackground(Color.DARK_GRAY);

        setLayout(null);

        setFocusable(true);

        addKeyListener(this);

        // =================================================
        // FOCUS LOST
        // =================================================

        addFocusListener(
            new FocusAdapter() {

                @Override
                public void focusLost(
                    FocusEvent e
                ) {

                    clearInput();
                }
            }
        );

        // =================================================
        // LOAD
        // =================================================

        loadMapImages();

        loadSprites();

        loadSounds();

        // =================================================
        // STATE
        // =================================================

        gameState = GameState.START;

        currentMap = MapType.MAIN;

        // =================================================
        // START BUTTON
        // =================================================

        startButton =
            new StyledButton(
                "START GAME",
                new Color(255, 214, 90),
                new Color(222, 140, 20)
            );

        startButton.setBounds(
            880,
            400,
            150,
            50
        );

        startButton.addActionListener(
            e -> startGame()
        );

        add(startButton);

        // =================================================
        // RESTART BUTTON
        // =================================================

        restartButton =
            new StyledButton(
                "RESTART",
                new Color(120, 222, 120),
                new Color(40, 140, 60)
            );

        restartButton.setBounds(
            810,
            500,
            130,
            45
        );

        restartButton.setVisible(false);

        restartButton.addActionListener(
            e -> startGame()
        );

        add(restartButton);

        // =================================================
        // EXIT BUTTON
        // =================================================

        exitButton =
            new StyledButton(
                "EXIT",
                new Color(235, 110, 110),
                new Color(150, 30, 40)
            );

        exitButton.setBounds(
            970,
            500,
            130,
            45
        );

        exitButton.setVisible(false);

        exitButton.addActionListener(
            e -> System.exit(0)
        );

        add(exitButton);

        // =================================================
        // GAME LOOP
        // =================================================

        timer =
            new Timer(
                16,
                e -> {

                    updateBgm();

                    update();

                    repaint();
                }
            );

        timer.start();
    }

    // =====================================================
    // SOUND
    // =====================================================

    private void loadSounds() {

        sound.setBgmVolume(BGM_VOLUME);

        sound.setSfxVolume(SFX_VOLUME);

        // ชื่อไฟล์ใน asset/Sound/SFX (ไม่รวม .mp3)
        sound.loadSfx(
            "CollectPowder",
            "Deliver",
            "doorclose",
            "gameover",
            "healing",
            "meleeattack",
            "Paidmom",
            "purchase",
            "shopdoorbell",
            "speedup",
            "ticket",
            "timestop"
        );
    }

    // เลือกเพลงพื้นหลังตามสถานะเกม / แมพ (เรียกทุกเฟรมได้ เพลงเดิมจะไม่เริ่มใหม่)
    private void updateBgm() {

        String track;

        if (gameState == GameState.START) {

            track = "StartMSC";

        } else if (gameState == GameState.GAME_OVER) {

            track = "GameOverMSC";

        } else if (currentMap == MapType.HOUSE) {

            track = "HouseMSC";

        } else if (currentMap == MapType.SHOP) {

            track = "ShopMSC";

        } else {

            track = "MainMSC";
        }

        sound.playBgm(track);
    }

    // =====================================================
    // CLEAR INPUT
    // =====================================================

    private void clearInput() {

        up = false;

        down = false;

        left = false;

        right = false;

        spacePressed = false;

        heldKeys.clear();
    }

    // =====================================================
    // MESSAGE
    // =====================================================

    private void showMessage(
        String text
    ) {

        message = text;

        messageEndTime =
            System.currentTimeMillis()
            + 2500;

        System.out.println(text);
    }

    // =====================================================
    // START GAME
    // =====================================================

    private void startGame() {

        player =
            new Player(
                900,
                500
            );

        rik =
            new Police(
                "Rik",
                300,
                300
            );

        gaffy =
            new Police(
                "Gaffy",
                1500,
                700
            );

        shop = new Shop();

        currentMap = MapType.MAIN;

        shopOpen = false;

        gameState =
            GameState.PLAYING;

        startTime =
            System.currentTimeMillis();

        lastScoreTime =
            startTime;

        lastMotherPaymentTime =
            startTime;

        motherPayment = 200;

        motherPaymentReady = false;

        resetCustomer();

        clearInput();

        playerDirection = "S";

        rikDirection = "S";

        gaffyDirection = "S";

        playerFrame = 0;

        rikFrame = 0;

        gaffyFrame = 0;

        playerAnimationTimer =
            startTime;

        rikAnimationTimer =
            startTime;

        gaffyAnimationTimer =
            startTime;

        rikAttackEndTime = 0;

        gaffyAttackEndTime = 0;

        interactAnimEndTime = 0;

        message = "";

        messageEndTime = 0;

        startButton.setVisible(false);

        restartButton.setVisible(false);

        exitButton.setVisible(false);

        requestFocusInWindow();
    }

    // =====================================================
    // UPDATE
    // =====================================================

    private void update() {

        if (
            gameState !=
            GameState.PLAYING
        ) {

            return;
        }

        if (shopOpen) {

            spacePressed = false;

            return;
        }

        // =================================================
        // PLAYER MOVEMENT
        // =================================================

        int dx = 0;

        int dy = 0;

        if (up) {

            dy -= 1;
        }

        if (down) {

            dy += 1;
        }

        if (left) {

            dx -= 1;
        }

        if (right) {

            dx += 1;
        }

        // =================================================
        // DIRECTION
        // =================================================

        if (dx < 0) {

            playerDirection = "A";

        } else if (dx > 0) {

            playerDirection = "D";

        } else if (dy < 0) {

            playerDirection = "W";

        } else if (dy > 0) {

            playerDirection = "S";
        }

        // =================================================
        // MOVE
        // =================================================

        if (
            dx != 0
            ||
            dy != 0
        ) {

            int oldX = player.getX();

            int oldY = player.getY();

            player.move(
                dx,
                dy
            );

            // ในบ้าน/ร้าน คูณระยะก้าวตามสเกลตัวละคร
            // (ยังรวมโบนัส Wing Shoes เหมือนเดิม)
            double scale = getPlayerScale();

            if (scale != 1.0) {

                int stepX = player.getX() - oldX;

                int stepY = player.getY() - oldY;

                player.setPosition(
                    oldX + (int) Math.round(stepX * scale),
                    oldY + (int) Math.round(stepY * scale)
                );
            }

            // ชนผนัง / สิ่งกีดขวาง (ทุกแมพ)
            resolveInteriorCollision(oldX, oldY);
        }

        keepPlayerInsideMap();

        // =================================================
        // MAP
        // =================================================

        if (
            currentMap ==
            MapType.MAIN
        ) {

            updateMainMap();

        } else if (
            currentMap ==
            MapType.HOUSE
        ) {

            updateHouse();

        } else if (
            currentMap ==
            MapType.SHOP
        ) {

            updateShop();
        }

        // =================================================
        // TIMER
        // =================================================

        updateMotherTimer();

        updateScore();

        // =================================================
        // GAME OVER
        // =================================================

        if (!player.isAlive()) {

            gameOver();
        }

        spacePressed = false;
    }

    // =====================================================
    // MAIN MAP
    // =====================================================

    private void updateMainMap() {

        updatePoliceWithCollision(rik);

        updatePoliceWithCollision(gaffy);

        checkPoliceHit(rik);

        checkPoliceHit(gaffy);

        if (!spacePressed) {

            return;
        }

        if (nearHouse()) {

            enterHouse();

        } else if (nearShop()) {

            enterShop();

        } else if (nearCustomer()) {

            deliverToCustomer();
        }
    }

    // =====================================================
    // POLICE UPDATE
    // =====================================================

    private void updatePoliceWithCollision(
        Police police
    ) {

        if (police == null) {

            return;
        }

        if (police.isStunned()) {

            return;
        }

        int oldX =
            police.getX();

        int oldY =
            police.getY();

        police.chase(player);

        int newX =
            police.getX();

        int newY =
            police.getY();

        // =================================================
        // COLLISION
        // =================================================

        if (
            !policeCanMoveTo(
                newX,
                newY
            )
        ) {

            if (
                policeCanMoveTo(
                    newX,
                    oldY
                )
            ) {

                police.moveTo(
                    newX,
                    oldY
                );

            } else if (
                policeCanMoveTo(
                    oldX,
                    newY
                )
            ) {

                police.moveTo(
                    oldX,
                    newY
                );

            } else {

                police.moveTo(
                    oldX,
                    oldY
                );

                police.changeRandomDirection();
            }
        }

        // =================================================
        // GET REAL DIRECTION
        // =================================================

        int finalX =
            police.getX();

        int finalY =
            police.getY();

        String direction = null;

        if (finalX < oldX) {

            direction = "A";

        } else if (finalX > oldX) {

            direction = "D";

        } else if (finalY < oldY) {

            direction = "W";

        } else if (finalY > oldY) {

            direction = "S";
        }

        if (direction != null) {

            if (police == rik) {

                rikDirection =
                    direction;

            } else {

                gaffyDirection =
                    direction;
            }
        }
    }

    // =====================================================
    // POLICE CAN MOVE
    // =====================================================

    private boolean policeCanMoveTo(
        int newX,
        int newY
    ) {

        Rectangle policeHitbox =
            new Rectangle(
                newX,
                newY,
                POLICE_HITBOX_SIZE,
                POLICE_HITBOX_SIZE
            );

        for (
            Rectangle wall :
            policeWalls
        ) {

            if (
                policeHitbox.intersects(
                    wall
                )
            ) {

                return false;
            }
        }

        // สิ่งกีดขวางในแมพหลัก: ใช้กล่องส่วนเท้าเหมือนผู้เล่น
        Rectangle policeFeet =
            new Rectangle(
                newX + 8,
                newY + 24,
                24,
                16
            );

        if (hitsMainWalls(policeFeet)) {

            return false;
        }

        return true;
    }

    // =====================================================
    // POLICE ATTACK
    // =====================================================

    private void checkPoliceHit(
        Police police
    ) {

        if (police == null) {

            return;
        }

        Rectangle playerHitbox =
            new Rectangle(
                player.getX(),
                player.getY(),
                PLAYER_HITBOX_SIZE,
                PLAYER_HITBOX_SIZE
            );

        Rectangle policeHitbox =
            new Rectangle(
                police.getX() - 15,
                police.getY() - 15,
                70,
                70
            );

        if (
            !playerHitbox.intersects(
                policeHitbox
            )
        ) {

            return;
        }

        int hpBefore =
            player.getHp();

        police.attack(player);

        if (
            player.getHp()
            <
            hpBefore
        ) {

            sound.playSfx("meleeattack");

            long end =
                System.currentTimeMillis()
                + 600;

            if (police == rik) {

                rikAttackEndTime =
                    end;

            } else {

                gaffyAttackEndTime =
                    end;
            }
        }
    }

    // =====================================================
    // HOUSE
    // =====================================================

    private void updateHouse() {

        if (!spacePressed) {

            return;
        }

        if (nearPowderSource()) {

            if (player.collectPowder()) {

                sound.playSfx("CollectPowder");

                interactAnimEndTime =
                    System.currentTimeMillis()
                    + 500;

                showMessage(
                    "Collected Powder! ("
                    + player.getPowder()
                    + "/"
                    + Player.MAX_POWDER
                    + ")"
                );

            } else {

                showMessage(
                    "You can only carry "
                    + Player.MAX_POWDER
                    + " Powder!"
                );
            }

        } else if (nearMother()) {

            payMother();

        } else if (nearHouseExit()) {

            exitHouse();
        }
    }

    // =====================================================
    // SHOP
    // =====================================================

    private void updateShop() {

        if (!spacePressed) {

            return;
        }

        if (nearShopItem()) {

            openShopMenu();

        } else if (nearShopExit()) {

            exitShop();
        }
    }

    // =====================================================
    // SHOP MENU
    // =====================================================

    private void openShopMenu() {

        shopOpen = true;

        up = false;

        down = false;

        left = false;

        right = false;

        requestFocusInWindow();
    }

    // =====================================================
    // ENTER / EXIT MAP
    // =====================================================

    private void enterHouse() {

        sound.playSfx("doorclose");

        currentMap =
            MapType.HOUSE;

        player.setPosition(
            900,
            700
        );
    }

    private void exitHouse() {

        sound.playSfx("doorclose");

        currentMap =
            MapType.MAIN;

        // ตำแหน่งเดิม
        player.setPosition(
            260,
            130
        );
    }

    private void enterShop() {

        sound.playSfx("shopdoorbell");

        currentMap =
            MapType.SHOP;

        player.setPosition(
            900,
            700
        );
    }

    private void exitShop() {

        sound.playSfx("shopdoorbell");

        currentMap =
            MapType.MAIN;

        // ตำแหน่งเดิม
        player.setPosition(
            1600,
            850
        );
    }

    // =====================================================
    // DELIVERY
    // =====================================================

    private void deliverToCustomer() {

        if (!player.hasPowder()) {

            showMessage(
                "You don't have Powder!"
            );

            return;
        }

        int moneyBefore =
            player.getMoney();

        player.deliverPowder();

        sound.playSfx("Deliver");

        player.addScore(6000);

        interactAnimEndTime =
            System.currentTimeMillis()
            + 500;

        String text =
            "Delivery successful! +"
            +
            (
                player.getMoney()
                -
                moneyBefore
            );

        customerDeliveriesLeft--;

        // ครบจำนวนครั้งของรอบนี้ = NPC ย้ายไปจุดใหม่
        if (customerDeliveriesLeft <= 0) {

            relocateCustomer();

            text += "  (Customer moved!)";
        }

        showMessage(text);
    }

    // =====================================================
    // MOTHER
    // =====================================================

    private void updateMotherTimer() {

        long elapsed =
            System.currentTimeMillis()
            -
            lastMotherPaymentTime;

        if (elapsed >= 120000) {

            motherPaymentReady = true;
        }
    }

    private void payMother() {

        if (!motherPaymentReady) {

            showMessage(
                "Mother doesn't need money yet."
            );

            return;
        }

        if (
            player.getMoney()
            <
            motherPayment
        ) {

            showMessage(
                "Not enough money to pay Mother!"
            );

            return;
        }

        player.spendMoney(
            motherPayment
        );

        sound.playSfx("Paidmom");

        player.addScore(20000);

        interactAnimEndTime =
            System.currentTimeMillis()
            + 500;

        lastMotherPaymentTime =
            System.currentTimeMillis();

        motherPaymentReady = false;

        showMessage(
            "Paid Mother "
            +
            motherPayment
            +
            "! +20000 score"
        );

        motherPayment =
            (int)
            (
                motherPayment
                *
                1.10
            );
    }

    // =====================================================
    // BUY ITEM
    // =====================================================

    private void buyItem(
        int itemNumber
    ) {

        boolean success = false;

        String name = "";

        if (itemNumber == 1) {

            success =
                shop.buyWatch(player);

            name =
                "Time Stop Watch";

        } else if (itemNumber == 2) {

            success =
                shop.buyShoes(player);

            name =
                "Wing Shoes";

        } else if (itemNumber == 3) {

            success =
                shop.buyTicket(player);

            name =
                "Powder Ticket";

        } else if (itemNumber == 4) {

            success =
                shop.buyVaccine(player);

            name =
                "Vaccine";
        }

        if (success) {

            sound.playSfx("purchase");

            showMessage(
                "Bought "
                +
                name
                +
                "! Money: "
                +
                player.getMoney()
            );

        } else {

            showMessage(
                "Not enough money!"
            );
        }
    }

    // =====================================================
    // USE ITEM
    // =====================================================

    private void useItem(
        int itemNumber
    ) {

        if (player == null) {

            return;
        }

        switch (itemNumber) {

            // =============================================
            // TIME STOP WATCH
            // =============================================

            case 1:

                if (!player.hasWatch()) {

                    showMessage(
                        "You don't have Time Stop Watch!"
                    );

                    return;
                }

                player.useWatch();

                sound.playSfx("timestop");

                rik.stun(10000);

                gaffy.stun(10000);

                showMessage(
                    "Time Stop Watch used! Police stopped 10 sec"
                );

                break;

            // =============================================
            // WING SHOES
            // =============================================

            case 2:

                if (!player.hasShoes()) {

                    showMessage(
                        "You don't have Wing Shoes!"
                    );

                    return;
                }

                if (
                    player.hasWingShoesEffect()
                ) {

                    showMessage(
                        "Wing Shoes is already active!"
                    );

                    return;
                }

                player.useShoes();

                sound.playSfx("speedup");

                showMessage(
                    "Wing Shoes used! Speed up 10 sec"
                );

                break;

            // =============================================
            // POWDER TICKET
            // =============================================

            case 3:

                if (!player.hasTicket()) {

                    showMessage(
                        "You don't have Powder Ticket!"
                    );

                    return;
                }

                if (
                    player.hasPowderTicket()
                ) {

                    showMessage(
                        "Powder Ticket is already active!"
                    );

                    return;
                }

                player.useTicket();

                sound.playSfx("ticket");

                showMessage(
                    "Powder Ticket used! +20% income 30 sec"
                );

                break;

            // =============================================
            // VACCINE
            // =============================================

            case 4:

                if (!player.hasVaccine()) {

                    showMessage(
                        "You don't have Vaccine!"
                    );

                    return;
                }

                if (
                    player.getHp()
                    >=
                    player.getMaxHp()
                ) {

                    showMessage(
                        "HP is already full!"
                    );

                    return;
                }

                player.useVaccine();

                sound.playSfx("healing");

                showMessage(
                    "Vaccine used! HP: "
                    +
                    player.getHp()
                );

                break;
        }
    }

    // =====================================================
    // SCORE
    // =====================================================

    private void updateScore() {

        // อยู่ในบ้าน/ร้านค้า = ไม่นับคะแนนเวลา (กันฟาร์มคะแนน)
        // รีเซ็ตตัวจับเวลาไว้ จึงไม่มีเวลาสะสมย้อนหลังตอนกลับออกมา
        if (currentMap != MapType.MAIN) {

            lastScoreTime =
                System.currentTimeMillis();

            return;
        }

        long elapsed =
            System.currentTimeMillis()
            -
            lastScoreTime;

        if (elapsed >= 1000) {

            int seconds =
                (int)
                (
                    elapsed
                    /
                    1000
                );

            player.addScore(
                seconds * 1000
            );

            lastScoreTime +=
                seconds * 1000L;
        }
    }

    // =====================================================
    // PLAYER BOUNDARY
    // =====================================================

    private void keepPlayerInsideMap() {

        int size =
            getPlayerHitboxSize();

        int x =
            player.getX();

        int y =
            player.getY();

        x =
            Math.max(
                0,
                Math.min(
                    MAP_WIDTH - size,
                    x
                )
            );

        y =
            Math.max(
                0,
                Math.min(
                    MAP_HEIGHT - size,
                    y
                )
            );

        player.setPosition(
            x,
            y
        );
    }

    // =====================================================
    // LOCATIONS
    // =====================================================

    private boolean nearHouse() {

        return distance(
            player.getX(),
            player.getY(),
            180,
            120
        ) < DOOR_RADIUS;
    }

    private boolean nearShop() {

        return distance(
            player.getX(),
            player.getY(),
            1660,
            850
        ) < DOOR_RADIUS;
    }

    private boolean nearCustomer() {

        return distance(
            player.getX(),
            player.getY(),
            customerX,
            customerY
        ) < INTERACT_RADIUS;
    }

    // =====================================================
    // INTERACT ZONES (ตำแหน่งตามรูป House.png / Store.png)
    // =====================================================

    // จุดเก็บผง = ลังที่ไฮไลต์สีส้มมุมขวาล่างของบ้าน
    private final Rectangle POWDER_ZONE =
        new Rectangle(1470, 685, 90, 70);

    // จุดซื้อของ = ไอคอนรถเข็นบนเคาน์เตอร์ร้านค้า
    private final Rectangle SHOP_COUNTER_ZONE =
        new Rectangle(1311, 280, 110, 110);

    // ปิดเป็น false ถ้าไม่อยากให้โชว์กรอบสี
    private final boolean SHOW_INTERACT_ZONES = true;

    // รัศมีจุดโต้ตอบ (วงมาร์คบนจอใช้ค่าเดียวกัน)
    private final int DOOR_RADIUS = 80;
    private final int INTERACT_RADIUS = 60;
    private final int EXIT_RADIUS = 70;

    // =====================================================
    // COLLISION (แมพบ้าน / ร้านค้า)
    // =====================================================

    // ตั้งเป็น true เพื่อดูกล่องชนสีแดงบนจอ (ไว้ปรับตำแหน่ง)
    private final boolean SHOW_COLLISION_BOXES = false;

    // รูป House.png / Store.png มีขนาด 1679x937 แต่เกมวาดที่ 1910x1000
    // จึงเขียนกล่องชนเป็นพิกัดบนรูปแล้วแปลงให้อัตโนมัติ
    private final double IMG_SCALE_X = MAP_WIDTH / 1679.0;
    private final double IMG_SCALE_Y = MAP_HEIGHT / 937.0;

    private Rectangle imgRect(int x, int y, int w, int h) {

        return new Rectangle(
            (int) Math.round(x * IMG_SCALE_X),
            (int) Math.round(y * IMG_SCALE_Y),
            (int) Math.round(w * IMG_SCALE_X),
            (int) Math.round(h * IMG_SCALE_Y)
        );
    }

    private final Rectangle[] houseWalls = {

        // ---- ผนัง ----
        imgRect(0, 0, 1679, 135),
        imgRect(0, 0, 200, 937),
        imgRect(0, 615, 562, 322),
        imgRect(562, 800, 148, 137),
        imgRect(925, 800, 754, 137),
        imgRect(1315, 0, 364, 240),
        imgRect(1480, 240, 199, 560),

        // ---- เฟอร์นิเจอร์ ----
        imgRect(205, 60, 280, 105),     // เตาผิง / ถัง / ต้นไม้
        imgRect(1150, 100, 160, 85),    // ตู้ลิ้นชัก
        imgRect(780, 118, 325, 230),    // โต๊ะ + เก้าอี้
        imgRect(200, 265, 290, 175),    // เตียง
        imgRect(205, 525, 340, 95),     // ถังแถวล่างซ้าย
        imgRect(1238, 283, 75, 90),     // ถังขวา
        imgRect(1315, 320, 165, 250),   // ตู้หนังสือ
        imgRect(1297, 635, 75, 165),    // ลังเก็บผง + ลังข้างล่าง
        imgRect(1380, 590, 100, 210)    // กองลังขวาล่าง
    };

    private final Rectangle[] shopWalls = {

        // ---- ผนัง ----
        imgRect(0, 0, 165, 937),
        imgRect(0, 0, 1040, 250),       // ตู้เย็น + ชั้นวางด้านบน
        imgRect(1040, 0, 640, 350),     // เคาน์เตอร์ + หลังร้าน
        imgRect(1415, 0, 264, 480),
        imgRect(1522, 480, 157, 457),
        imgRect(0, 630, 465, 307),
        imgRect(465, 750, 180, 187),
        imgRect(865, 745, 814, 192),

        // ---- ชั้นวางของ ----
        imgRect(215, 370, 125, 175),    // แผงผลไม้
        imgRect(165, 545, 110, 90),     // ต้นไม้ + ถัง
        imgRect(530, 322, 125, 258),    // ชั้นกลางซ้าย
        imgRect(798, 322, 125, 258),    // ชั้นกลางขวา
        imgRect(1275, 420, 128, 140),   // ชั้นขวา
        imgRect(1165, 630, 340, 110)    // ตู้แช่ + ตะกร้า
    };

    // =====================================================
    // MAIN MAP WALLS : ใช้ร่วมกันทั้งผู้เล่นและตำรวจ
    // =====================================================
    // เพิ่มสิ่งกีดขวางเป็น new Rectangle(x, y, กว้าง, สูง)
    // พิกัดเป็นพิกเซลของแมพ (1910x1000) ไม่ใช่พิกัดบนรูป
    // ควรมีน้อยๆ และเว้นช่องทางให้วิ่งหลบตำรวจได้ (ช่องกว้าง >= 120)

    private final Rectangle[] mainWalls = {
    };

    private boolean hitsMainWalls(Rectangle box) {

        for (Rectangle wall : mainWalls) {

            if (box.intersects(wall)) {

                return true;
            }
        }

        return false;
    }

    private Rectangle[] getCurrentWalls() {

        if (currentMap == MapType.MAIN) {

            return mainWalls;
        }

        if (currentMap == MapType.HOUSE) {

            return houseWalls;
        }

        if (currentMap == MapType.SHOP) {

            return shopWalls;
        }

        return new Rectangle[0];
    }

    // กล่องชนของผู้เล่นคือ "ส่วนเท้า" (ล่างของตัว) เพื่อให้เดินชิดเฟอร์นิเจอร์ได้สมจริง
    private Rectangle getPlayerFeetBox(int x, int y) {

        int size = getPlayerHitboxSize();

        int w = (int) (size * 0.6);

        int h = (int) (size * 0.4);

        return new Rectangle(
            x + (size - w) / 2,
            y + size - h,
            w,
            h
        );
    }

    private boolean hitsWall(int x, int y) {

        Rectangle feet = getPlayerFeetBox(x, y);

        for (Rectangle wall : getCurrentWalls()) {

            if (feet.intersects(wall)) {

                return true;
            }
        }

        return false;
    }

    private void resolveInteriorCollision(int oldX, int oldY) {

        int newX = player.getX();

        int newY = player.getY();

        if (!hitsWall(newX, newY)) {

            return;
        }

        // กันติด: ถ้าตำแหน่งเดิมก็ชนอยู่แล้ว ให้เดินออกได้
        if (hitsWall(oldX, oldY)) {

            return;
        }

        // ไถลไปตามผนัง
        if (!hitsWall(newX, oldY)) {

            player.setPosition(newX, oldY);

            return;
        }

        if (!hitsWall(oldX, newY)) {

            player.setPosition(oldX, newY);

            return;
        }

        player.setPosition(oldX, oldY);
    }

    private void drawCollisionBoxes(Graphics g) {

        g.setColor(new Color(255, 0, 0, 70));

        for (Rectangle wall : getCurrentWalls()) {

            g.fillRect(wall.x, wall.y, wall.width, wall.height);
        }

        Rectangle feet =
            getPlayerFeetBox(player.getX(), player.getY());

        g.setColor(new Color(0, 255, 0, 120));

        g.fillRect(feet.x, feet.y, feet.width, feet.height);
    }

    // =====================================================
    // CUSTOMER (NPC) : ตำแหน่งสุ่ม + จำนวนครั้งที่จ่ายได้ต่อรอบ
    // =====================================================

    private final java.util.Random random =
        new java.util.Random();

    private int customerX = 1000;
    private int customerY = 500;

    // จ่ายได้อีกกี่ครั้งก่อนที่ NPC จะย้ายที่ (สุ่ม 1-3 ต่อรอบ)
    private int customerDeliveriesLeft = 1;

    // ขอบเขตที่ NPC สุ่มไปยืนได้ (ของแมพหลัก)
    private final int CUSTOMER_MIN_X = 150;
    private final int CUSTOMER_MAX_X = 1750;
    private final int CUSTOMER_MIN_Y = 150;
    private final int CUSTOMER_MAX_Y = 850;

    private void resetCustomer() {

        customerX = 1000;

        customerY = 500;

        customerDeliveriesLeft =
            1 + random.nextInt(3);
    }

    private void relocateCustomer() {

        int oldX = customerX;

        int oldY = customerY;

        for (int i = 0; i < 100; i++) {

            int x =
                CUSTOMER_MIN_X
                + random.nextInt(
                    CUSTOMER_MAX_X - CUSTOMER_MIN_X + 1
                );

            int y =
                CUSTOMER_MIN_Y
                + random.nextInt(
                    CUSTOMER_MAX_Y - CUSTOMER_MIN_Y + 1
                );

            // ไกลจากจุดเดิม
            if (distance(x, y, oldX, oldY) < 400) {
                continue;
            }

            // ไม่ทับประตูบ้าน / ร้านค้า
            if (distance(x, y, 180, 120) < 300) {
                continue;
            }

            if (distance(x, y, 1660, 850) < 300) {
                continue;
            }

            // ไม่เกิดในสิ่งกีดขวาง
            if (
                hitsMainWalls(
                    new Rectangle(x - 10, y - 10, 60, 60)
                )
            ) {
                continue;
            }

            // ไม่เกิดติดตัวผู้เล่น
            if (
                distance(
                    x,
                    y,
                    player.getX(),
                    player.getY()
                ) < 250
            ) {
                continue;
            }

            customerX = x;

            customerY = y;

            break;
        }

        customerDeliveriesLeft =
            1 + random.nextInt(3);
    }

    // =====================================================
    // PLAYER SCALE (ในบ้าน / ร้านค้า ตัวละครใหญ่ขึ้นตามสัดส่วนแมพ)
    // =====================================================

    // ปรับเลขนี้ได้: มากขึ้น = ตัวใหญ่และเร็วขึ้นในบ้าน/ร้าน
    private final double INTERIOR_SCALE = 2.2;

    private boolean isInterior() {

        return currentMap == MapType.HOUSE
            || currentMap == MapType.SHOP;
    }

    private double getPlayerScale() {

        return isInterior() ? INTERIOR_SCALE : 1.0;
    }

    private int getPlayerHitboxSize() {

        return (int) Math.round(
            PLAYER_HITBOX_SIZE * getPlayerScale()
        );
    }

    private Rectangle getPlayerHitbox() {

        int size = getPlayerHitboxSize();

        return new Rectangle(
            player.getX(),
            player.getY(),
            size,
            size
        );
    }

    private boolean nearPowderSource() {

        return getPlayerHitbox().intersects(POWDER_ZONE);
    }

    private boolean nearMother() {

        return distance(
            player.getX(),
            player.getY(),
            1100,
            300
        ) < INTERACT_RADIUS;
    }

    private boolean nearHouseExit() {

        return distance(
            player.getX(),
            player.getY(),
            900,
            850
        ) < EXIT_RADIUS;
    }

    private boolean nearShopItem() {

        return getPlayerHitbox().intersects(SHOP_COUNTER_ZONE);
    }

    private boolean nearShopExit() {

        return distance(
            player.getX(),
            player.getY(),
            900,
            850
        ) < EXIT_RADIUS;
    }

    private double distance(
        int x1,
        int y1,
        int x2,
        int y2
    ) {

        int dx =
            x1 - x2;

        int dy =
            y1 - y2;

        return Math.sqrt(
            dx * dx
            +
            dy * dy
        );
    }

    // =====================================================
    // GAME OVER
    // =====================================================

    private void gameOver() {

        if (gameState != GameState.GAME_OVER) {

            sound.playSfx("gameover");
        }

        gameState =
            GameState.GAME_OVER;

        shopOpen = false;

        gameEndTime =
            System.currentTimeMillis();

        restartButton.setVisible(true);

        exitButton.setVisible(true);

        clearInput();
    }

    // =====================================================
    // PAINT
    // =====================================================

    @Override
    protected void paintComponent(
        Graphics g
    ) {

        super.paintComponent(g);

        // =================================================
        // START SCREEN
        // =================================================

        if (
            gameState ==
            GameState.START
        ) {

            drawStartScreen(g);

            return;
        }

        if (player == null) {

            return;
        }

        // =================================================
        // MAP
        // =================================================

        if (
            currentMap ==
            MapType.MAIN
        ) {

            drawMap(
                g,
                mainMapImage
            );

        } else if (
            currentMap ==
            MapType.HOUSE
        ) {

            drawMap(
                g,
                houseMapImage
            );

            drawMotherNotice(g);

        } else if (
            currentMap ==
            MapType.SHOP
        ) {

            drawMap(
                g,
                storeMapImage
            );
        }

        // =================================================
        // INTERACT ZONES
        // =================================================

        if (SHOW_INTERACT_ZONES) {

            drawInteractZones(g);
        }

        // =================================================
        // MOM / NPC
        // =================================================

        if (currentMap == MapType.HOUSE) {

            // ตำแหน่งเดียวกับ nearMother()
            drawStaticCharacter(
                g,
                momStand,
                momImage,
                1100,
                300,
                INTERIOR_SCALE,
                "Mom"
            );

        } else if (currentMap == MapType.MAIN) {

            // ตำแหน่งเดียวกับ nearCustomer()
            drawStaticCharacter(
                g,
                npcStand,
                npcImage,
                customerX,
                customerY,
                1.0,
                "Customer"
            );
        }

        if (SHOW_COLLISION_BOXES) {

            drawCollisionBoxes(g);
        }

        // =================================================
        // PLAYER
        // =================================================

        drawPlayer(g);

        // =================================================
        // POLICE
        // =================================================

        if (
            currentMap ==
            MapType.MAIN
        ) {

            drawPolice(
                g,
                rik,
                Color.RED
            );

            drawPolice(
                g,
                gaffy,
                Color.ORANGE
            );
        }

        // =================================================
        // UI
        // =================================================

        drawUI(g);

        // =================================================
        // SHOP
        // =================================================

        if (shopOpen) {

            drawShopMenu(g);
        }

        // =================================================
        // MESSAGE
        // =================================================

        drawMessage(g);

        // =================================================
        // GAME OVER
        // =================================================

        if (
            gameState ==
            GameState.GAME_OVER
        ) {

            drawGameOver(g);
        }
    }

    // =====================================================
    // DRAW INTERACT ZONES
    // =====================================================

    // สีพาสเทลอ่อน แยกแต่ละจุด
    private final Color ZONE_HOUSE_DOOR = new Color(255, 240, 150);
    private final Color ZONE_SHOP_DOOR = new Color(255, 190, 215);
    private final Color ZONE_CUSTOMER = new Color(255, 160, 160);
    private final Color ZONE_POWDER = new Color(150, 230, 150);
    private final Color ZONE_MOTHER = new Color(225, 165, 235);
    private final Color ZONE_HOUSE_EXIT = new Color(160, 185, 255);
    private final Color ZONE_SHOP_COUNTER = new Color(255, 205, 140);
    private final Color ZONE_SHOP_EXIT = new Color(150, 230, 240);

    private void drawInteractZones(Graphics g) {

        if (currentMap == MapType.MAIN) {

            drawCircleZone(g, 180, 120, DOOR_RADIUS, ZONE_HOUSE_DOOR, "HOUSE");

            drawCircleZone(g, 1660, 850, DOOR_RADIUS, ZONE_SHOP_DOOR, "SHOP");

            drawCircleZone(
                g,
                customerX,
                customerY,
                INTERACT_RADIUS,
                ZONE_CUSTOMER,
                "DELIVER"
            );

        } else if (currentMap == MapType.HOUSE) {

            drawRectZone(g, POWDER_ZONE, ZONE_POWDER, "POWDER");

            drawCircleZone(g, 1100, 300, INTERACT_RADIUS, ZONE_MOTHER, "MOM");

            drawCircleZone(g, 900, 850, EXIT_RADIUS, ZONE_HOUSE_EXIT, "EXIT");

        } else if (currentMap == MapType.SHOP) {

            drawRectZone(g, SHOP_COUNTER_ZONE, ZONE_SHOP_COUNTER, "BUY");

            drawCircleZone(g, 900, 850, EXIT_RADIUS, ZONE_SHOP_EXIT, "EXIT");
        }
    }

    private void drawRectZone(
        Graphics g,
        Rectangle zone,
        Color color,
        String label
    ) {

        g.setColor(
            new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                40
            )
        );

        g.fillRoundRect(
            zone.x,
            zone.y,
            zone.width,
            zone.height,
            16,
            16
        );

        g.setColor(
            new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                150
            )
        );

        ((Graphics2D) g).setStroke(new BasicStroke(2));

        g.drawRoundRect(
            zone.x,
            zone.y,
            zone.width,
            zone.height,
            16,
            16
        );

        drawZoneLabel(g, label, zone.x + 4, zone.y - 6, color);
    }

    private void drawCircleZone(
        Graphics g,
        int targetX,
        int targetY,
        int radius,
        Color color,
        String label
    ) {

        int cx = targetX + getPlayerHitboxSize() / 2;
        int cy = targetY + getPlayerHitboxSize() / 2;

        g.setColor(
            new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                40
            )
        );

        g.fillOval(
            cx - radius,
            cy - radius,
            radius * 2,
            radius * 2
        );

        g.setColor(
            new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                150
            )
        );

        ((Graphics2D) g).setStroke(new BasicStroke(2));

        g.drawOval(
            cx - radius,
            cy - radius,
            radius * 2,
            radius * 2
        );

        drawZoneLabel(g, label, cx - 25, cy - radius - 6, color);
    }

    private void drawZoneLabel(
        Graphics g,
        String label,
        int x,
        int y,
        Color color
    ) {

        g.setFont(new Font("Arial", Font.BOLD, 13));

        g.setColor(new Color(0, 0, 0, 150));

        g.drawString(label, x + 1, y + 1);

        g.setColor(color);

        g.drawString(label, x, y);
    }

    // =====================================================
    // DRAW STATIC CHARACTER (แม่ / NPC)
    // =====================================================

    private void drawStaticCharacter(
        Graphics g,
        List<BufferedImage> animation,
        BufferedImage fallbackImage,
        int x,
        int y,
        double scale,
        String label
    ) {

        BufferedImage frame =
            getFrame(
                animation,
                (int) (System.currentTimeMillis() / 200)
            );

        if (frame == null) {

            frame = fallbackImage;
        }

        int hitboxSize =
            (int) Math.round(
                PLAYER_HITBOX_SIZE * scale
            );

        if (frame != null) {

            drawCharacterSprite(
                g,
                frame,
                x,
                y,
                (int) Math.round(PLAYER_SPRITE_WIDTH * scale),
                (int) Math.round(PLAYER_SPRITE_HEIGHT * scale),
                hitboxSize
            );
        }

        g.setColor(Color.WHITE);

        g.setFont(
            new Font(
                "Arial",
                Font.PLAIN,
                12
            )
        );

        g.drawString(
            label,
            x,
            y - 5
        );
    }

    // =====================================================
    // DRAW MAP
    // =====================================================

    private void drawMap(
        Graphics g,
        BufferedImage image
    ) {

        if (image != null) {

            g.drawImage(
                image,
                0,
                0,
                MAP_WIDTH,
                MAP_HEIGHT,
                null
            );

        } else {

            g.setColor(
                Color.DARK_GRAY
            );

            g.fillRect(
                0,
                0,
                MAP_WIDTH,
                MAP_HEIGHT
            );
        }
    }

    // =====================================================
    // MOTHER NOTICE
    // =====================================================

    private void drawMotherNotice(
        Graphics g
    ) {

        if (!motherPaymentReady) {

            return;
        }

        g.setColor(Color.RED);

        g.setFont(
            new Font(
                "Arial",
                Font.BOLD,
                20
            )
        );

        g.drawString(
            "MOTHER PAYMENT READY",
            850,
            100
        );

        g.drawString(
            "Amount: "
            +
            motherPayment,
            900,
            125
        );
    }

    // =====================================================
    // PLAYER ANIMATION
    // =====================================================

    private List<BufferedImage>
    getPlayerMoveAnimation() {

        switch (playerDirection) {

            case "A":
                return playerMoveA;

            case "D":
                return playerMoveD;

            case "W":
                return playerMoveW;

            default:
                return playerMoveS;
        }
    }

    // =====================================================
    // DRAW PLAYER
    // =====================================================

    private void drawPlayer(
        Graphics g
    ) {

        long now =
            System.currentTimeMillis();

        boolean moving =
            (left != right)
            ||
            (up != down);

        List<BufferedImage>
            animation;

        // =================================================
        // SELECT ANIMATION
        // =================================================

        if (
            gameState ==
            GameState.GAME_OVER
        ) {

            animation =
                playerGameOver;

        } else if (
            now <
            interactAnimEndTime
            &&
            !playerDelivery.isEmpty()
        ) {

            animation =
                playerDelivery;

        } else if (moving) {

            animation =
                getPlayerMoveAnimation();

        } else {

            animation =
                playerStand;
        }

        // =================================================
        // UPDATE FRAME
        // =================================================

        if (
            now
            -
            playerAnimationTimer
            >=
            ANIMATION_DELAY
        ) {

            playerFrame++;

            playerAnimationTimer =
                now;
        }

        // =================================================
        // INVINCIBLE BLINK
        // =================================================

        if (
            gameState ==
            GameState.PLAYING
            &&
            player.isInvincible()
            &&
            (now / 150) % 2 == 0
        ) {

            return;
        }

        BufferedImage frame =
            getFrame(
                animation,
                playerFrame
            );

        // =================================================
        // DRAW WITH ASPECT RATIO
        // =================================================

        if (frame != null) {

            double scale = getPlayerScale();

            drawCharacterSprite(
                g,
                frame,
                player.getX(),
                player.getY(),
                (int) Math.round(PLAYER_SPRITE_WIDTH * scale),
                (int) Math.round(PLAYER_SPRITE_HEIGHT * scale),
                getPlayerHitboxSize()
            );

        } else {

            double scale = getPlayerScale();

            int box = (int) Math.round(32 * scale);

            int off = (getPlayerHitboxSize() - box) / 2;

            g.setColor(Color.WHITE);

            g.fillRect(
                player.getX() + off,
                player.getY() + off,
                box,
                box
            );
        }
    }

    // =====================================================
    // POLICE ANIMATION
    // =====================================================

    private List<BufferedImage>
    getPoliceAnimation(
        boolean isRik,
        boolean running,
        String direction
    ) {

        if (isRik) {

            switch (direction) {

                case "A":

                    return running
                        ? RRunA
                        : RMoveA;

                case "D":

                    return running
                        ? RRunD
                        : RMoveD;

                case "W":

                    return running
                        ? RRunW
                        : RMoveW;

                default:

                    return running
                        ? RRunS
                        : RMoveS;
            }
        }

        switch (direction) {

            case "A":

                return running
                    ? gaffyRunA
                    : gaffyMoveA;

            case "D":

                return running
                    ? gaffyRunD
                    : gaffyMoveD;

            case "W":

                return running
                    ? gaffyRunW
                    : gaffyMoveW;

            default:

                return running
                    ? gaffyRunS
                    : gaffyMoveS;
        }
    }

    // =====================================================
    // DRAW POLICE
    // =====================================================

    private void drawPolice(
        Graphics g,
        Police police,
        Color fallbackColor
    ) {

        if (police == null) {

            return;
        }

        long now =
            System.currentTimeMillis();

        boolean isRik =
            (police == rik);

        String direction =
            isRik
                ? rikDirection
                : gaffyDirection;

        long attackEnd =
            isRik
                ? rikAttackEndTime
                : gaffyAttackEndTime;

        boolean attacking =
            now < attackEnd;

        // =================================================
        // RUNNING / WALKING
        // =================================================

        boolean running =
            distance(
                police.getX(),
                police.getY(),
                player.getX(),
                player.getY()
            )
            <=
            police.getChaseRange();

        List<BufferedImage>
            animation;

        if (attacking) {

            animation =
                isRik
                    ? RAttack
                    : gaffyAttack;

        } else {

            animation =
                getPoliceAnimation(
                    isRik,
                    running,
                    direction
                );

            // ถ้าไม่มี animation เดิน
            // ให้ใช้ animation วิ่งแทน

            if (
                animation == null
                ||
                animation.isEmpty()
            ) {

                animation =
                    getPoliceAnimation(
                        isRik,
                        true,
                        direction
                    );
            }
        }

        // =================================================
        // UPDATE RIK FRAME
        // =================================================

        if (isRik) {

            if (
                now
                -
                rikAnimationTimer
                >=
                ANIMATION_DELAY
            ) {

                if (!police.isStunned()) {

                    rikFrame++;
                }

                rikAnimationTimer =
                    now;
            }

        } else {

            // =================================================
            // UPDATE GAFFY FRAME
            // =================================================

            if (
                now
                -
                gaffyAnimationTimer
                >=
                ANIMATION_DELAY
            ) {

                if (!police.isStunned()) {

                    gaffyFrame++;
                }

                gaffyAnimationTimer =
                    now;
            }
        }

        // =================================================
        // GET FRAME
        // =================================================

        BufferedImage frame =
            getFrame(
                animation,
                isRik
                    ? rikFrame
                    : gaffyFrame
            );

        // =================================================
        // DRAW SPRITE
        // =================================================

        if (frame != null) {

            drawCharacterSprite(
                g,
                frame,
                police.getX(),
                police.getY(),
                POLICE_SPRITE_WIDTH,
                POLICE_SPRITE_HEIGHT
            );

        } else {

            g.setColor(
                fallbackColor
            );

            g.fillRect(
                police.getX() + 4,
                police.getY() + 4,
                32,
                32
            );
        }

        // =================================================
        // NAME
        // =================================================

        g.setColor(Color.WHITE);

        g.setFont(
            new Font(
                "Arial",
                Font.PLAIN,
                12
            )
        );

        g.drawString(
            police.getName(),
            police.getX(),
            police.getY() - 5
        );

        // =================================================
        // STUN
        // =================================================

        if (police.isStunned()) {

            g.setColor(Color.CYAN);

            g.drawString(
                "STUN",
                police.getX(),
                police.getY() + 45
            );
        }
    }

    // =====================================================
    // DRAW UI
    // =====================================================

    private void drawUI(
        Graphics g
    ) {

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        drawStatusPanel(g2);

        drawScoreBoard(g2);

        drawActiveEffects(g2);

        drawItemBar(g2);

        // =================================================
        // CONTROL
        // =================================================

        g2.setColor(Color.WHITE);

        g2.setFont(
            new Font(
                "Arial",
                Font.PLAIN,
                14
            )
        );

        g2.drawString(
            "WASD = Move",
            20,
            930
        );

        g2.drawString(
            "SPACE = Interact",
            20,
            950
        );

        g2.drawString(
            "1-4 = Use Item",
            20,
            970
        );
    }

    // =====================================================
    // UI HELPERS
    // =====================================================

    private void drawShadowText(
        Graphics2D g2,
        String text,
        int x,
        int y,
        Color color
    ) {

        g2.setColor(new Color(0, 0, 0, 170));

        g2.drawString(text, x + 2, y + 2);

        g2.setColor(color);

        g2.drawString(text, x, y);
    }

    private void drawCenteredText(
        Graphics2D g2,
        String text,
        int centerX,
        int y,
        Color color
    ) {

        int width =
            g2.getFontMetrics().stringWidth(text);

        drawShadowText(
            g2,
            text,
            centerX - width / 2,
            y,
            color
        );
    }

    // วาดรูปให้พอดีกรอบโดยคงสัดส่วน (ถ้าไม่มีรูปวาดกล่องสำรอง)
    private void drawImageFit(
        Graphics2D g2,
        BufferedImage image,
        int x,
        int y,
        int w,
        int h,
        String fallbackLetter
    ) {

        if (image == null) {

            g2.setColor(new Color(120, 120, 120, 200));

            g2.fillRoundRect(x, y, w, h, 14, 14);

            g2.setColor(Color.WHITE);

            g2.setFont(
                new Font("Arial", Font.BOLD, h / 2)
            );

            drawCenteredText(
                g2,
                fallbackLetter,
                x + w / 2,
                y + h * 2 / 3,
                Color.WHITE
            );

            return;
        }

        double scale =
            Math.min(
                (double) w / image.getWidth(),
                (double) h / image.getHeight()
            );

        int dw = (int) (image.getWidth() * scale);

        int dh = (int) (image.getHeight() * scale);

        g2.drawImage(
            image,
            x + (w - dw) / 2,
            y + (h - dh) / 2,
            dw,
            dh,
            null
        );
    }

    // =====================================================
    // STATUS PANEL (MAP / HP / MONEY / POWDER / TIME)
    // =====================================================

    private void drawStatusPanel(Graphics2D g2) {

        int px = 15;
        int py = 15;
        int pw = 300;
        int ph = 175;

        g2.setColor(new Color(15, 15, 20, 175));

        g2.fillRoundRect(px, py, pw, ph, 22, 22);

        g2.setColor(new Color(255, 255, 255, 60));

        g2.setStroke(new BasicStroke(2));

        g2.drawRoundRect(px, py, pw, ph, 22, 22);

        // ---- MAP ----
        g2.setFont(new Font("Arial", Font.BOLD, 13));

        drawShadowText(
            g2,
            "MAP: " + currentMap,
            px + 14,
            py + 24,
            new Color(200, 210, 230)
        );

        // ---- HP BAR ----
        drawHpBar(g2, px + 12, py + 36, pw - 24, 28);

        // ---- MONEY ----
        g2.setFont(new Font("Arial", Font.BOLD, 19));

        drawShadowText(
            g2,
            "Money: " + player.getMoney(),
            px + 14,
            py + 94,
            new Color(255, 215, 90)
        );

        // ---- POWDER ----
        g2.setFont(new Font("Arial", Font.BOLD, 16));

        drawShadowText(
            g2,
            "Powder: "
            + player.getPowder()
            + "/"
            + Player.MAX_POWDER,
            px + 14,
            py + 124,
            player.getPowder() >= Player.MAX_POWDER
                ? new Color(255, 140, 120)
                : Color.WHITE
        );

        // ---- TIME ----
        long seconds =
            (System.currentTimeMillis() - startTime) / 1000;

        g2.setFont(new Font("Arial", Font.PLAIN, 14));

        drawShadowText(
            g2,
            "Time: " + seconds + " sec",
            px + 14,
            py + 152,
            new Color(190, 190, 200)
        );
    }

    // =====================================================
    // HP BAR (แถบแดงขอบมน)
    // =====================================================

    private void drawHpBar(
        Graphics2D g2,
        int x,
        int y,
        int w,
        int h
    ) {

        double ratio =
            Math.max(
                0.0,
                Math.min(
                    1.0,
                    (double) player.getHp() / player.getMaxHp()
                )
            );

        // พื้นหลังหลอด
        g2.setColor(new Color(45, 10, 12));

        g2.fillRoundRect(x, y, w, h, h, h);

        // เนื้อหลอด (ตัดตามขอบมน)
        Shape oldClip = g2.getClip();

        g2.setClip(
            new java.awt.geom.RoundRectangle2D.Float(
                x, y, w, h, h, h
            )
        );

        int fillWidth = (int) Math.round(w * ratio);

        if (fillWidth > 0) {

            g2.setPaint(
                new GradientPaint(
                    0, y, new Color(255, 85, 85),
                    0, y + h, new Color(170, 10, 20)
                )
            );

            g2.fillRect(x, y, fillWidth, h);

            // แถบแสงด้านบน
            g2.setColor(new Color(255, 255, 255, 70));

            g2.fillRect(x, y + 3, fillWidth, h / 4);
        }

        g2.setClip(oldClip);

        // ขอบ
        g2.setColor(new Color(255, 255, 255, 190));

        g2.setStroke(new BasicStroke(2));

        g2.drawRoundRect(x, y, w, h, h, h);

        // ตัวเลข
        g2.setFont(new Font("Arial", Font.BOLD, 15));

        drawCenteredText(
            g2,
            "HP  "
            + player.getHp()
            + " / "
            + player.getMaxHp(),
            x + w / 2,
            y + h / 2 + 5,
            Color.WHITE
        );
    }

    // =====================================================
    // SCORE BOARD (มุมขวาบน ตัวใหญ่เด่น)
    // =====================================================

    private void drawScoreBoard(Graphics2D g2) {

        int pw = 320;
        int ph = 92;
        int px = getWidth() - pw - 20;
        int py = 15;

        g2.setColor(new Color(20, 14, 4, 205));

        g2.fillRoundRect(px, py, pw, ph, 26, 26);

        g2.setColor(new Color(255, 200, 60));

        g2.setStroke(new BasicStroke(3));

        g2.drawRoundRect(px, py, pw, ph, 26, 26);

        g2.setFont(new Font("Arial", Font.BOLD, 16));

        drawCenteredText(
            g2,
            "S C O R E",
            px + pw / 2,
            py + 28,
            new Color(255, 225, 140)
        );

        g2.setFont(new Font("Arial", Font.BOLD, 48));

        drawCenteredText(
            g2,
            String.format("%,d", player.getScore()),
            px + pw / 2,
            py + 76,
            new Color(255, 215, 0)
        );
    }

    // =====================================================
    // ACTIVE EFFECTS (ใต้แผงสถานะ)
    // =====================================================

    private void drawActiveEffects(Graphics2D g2) {

        int x = 20;
        int y = 225;

        g2.setFont(new Font("Arial", Font.BOLD, 15));

        if (player.hasPowderTicket()) {

            drawShadowText(
                g2,
                String.format(
                    "POWDER TICKET: %.1f sec",
                    player.getPowderTicketRemaining() / 1000.0
                ),
                x,
                y,
                new Color(120, 255, 140)
            );

            y += 24;
        }

        if (player.hasWingShoesEffect()) {

            drawShadowText(
                g2,
                String.format(
                    "WING SHOES: %.1f sec",
                    player.getWingShoesRemaining() / 1000.0
                ),
                x,
                y,
                new Color(120, 235, 255)
            );

            y += 24;
        }

        if (player.isInvincible()) {

            drawShadowText(
                g2,
                String.format(
                    "INVINCIBLE: %.1f",
                    player.getInvincibleRemaining() / 1000.0
                ),
                x,
                y,
                new Color(255, 240, 120)
            );

            y += 24;
        }

        if (motherPaymentReady) {

            drawShadowText(
                g2,
                "MOTHER PAYMENT: " + motherPayment,
                x,
                y,
                new Color(255, 110, 110)
            );
        }
    }

    // =====================================================
    // ITEM BAR (กลางล่างจอ)
    // =====================================================

    private void drawItemBar(Graphics2D g2) {

        int slot = 84;
        int gap = 14;
        int total = 4 * slot + 3 * gap;

        int startX = (getWidth() - total) / 2;
        int y = getHeight() - slot - 26;

        for (int i = 0; i < 4; i++) {

            int x = startX + i * (slot + gap);

            int count = getItemCount(i);

            // เอฟเฟกต์ที่กำลังทำงาน (0 = ไม่มี)
            double activeRatio = 0;

            if (i == 1 && player.hasWingShoesEffect()) {

                activeRatio =
                    player.getWingShoesRemaining() / 10000.0;

            } else if (i == 2 && player.hasPowderTicket()) {

                activeRatio =
                    player.getPowderTicketRemaining() / 30000.0;
            }

            boolean active = activeRatio > 0;

            // ---- กรอบช่อง ----
            g2.setColor(new Color(18, 14, 10, 205));

            g2.fillRoundRect(x, y, slot, slot, 20, 20);

            Color border =
                active
                    ? new Color(110, 255, 150)
                    : count > 0
                        ? new Color(255, 205, 80)
                        : new Color(120, 120, 120);

            g2.setColor(border);

            g2.setStroke(new BasicStroke(active ? 4 : 3));

            g2.drawRoundRect(x, y, slot, slot, 20, 20);

            // ---- ไอคอน (จางลงถ้าไม่มีของ) ----
            Composite oldComposite = g2.getComposite();

            if (count == 0) {

                g2.setComposite(
                    AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        0.35f
                    )
                );
            }

            drawImageFit(
                g2,
                itemImages[i],
                x + 12,
                y + 10,
                slot - 24,
                slot - 32,
                ITEM_NAMES[i].substring(0, 1)
            );

            g2.setComposite(oldComposite);

            // ---- ปุ่มลัด (มุมซ้ายบน) ----
            g2.setColor(new Color(255, 205, 80));

            g2.fillOval(x - 6, y - 6, 26, 26);

            g2.setFont(new Font("Arial", Font.BOLD, 15));

            g2.setColor(new Color(40, 25, 5));

            g2.drawString(
                String.valueOf(i + 1),
                x + 3,
                y + 13
            );

            // ---- จำนวน (มุมขวาล่าง) ----
            g2.setFont(new Font("Arial", Font.BOLD, 17));

            drawShadowText(
                g2,
                "x" + count,
                x + slot - 12
                    - g2.getFontMetrics().stringWidth("x" + count),
                y + slot - 8,
                count > 0 ? Color.WHITE : new Color(160, 160, 160)
            );

            // ---- แถบเวลาเอฟเฟกต์ ----
            if (active) {

                int barW = slot - 16;

                g2.setColor(new Color(0, 0, 0, 170));

                g2.fillRoundRect(x + 8, y + slot + 5, barW, 8, 8, 8);

                g2.setColor(new Color(110, 255, 150));

                g2.fillRoundRect(
                    x + 8,
                    y + slot + 5,
                    (int) (barW * Math.min(1.0, activeRatio)),
                    8,
                    8,
                    8
                );
            }
        }
    }

    // =====================================================
    // MESSAGE
    // =====================================================

    private void drawMessage(
        Graphics g
    ) {

        if (
            System.currentTimeMillis()
            >=
            messageEndTime
            ||
            message.isEmpty()
        ) {

            return;
        }

        g.setFont(
            new Font(
                "Arial",
                Font.BOLD,
                24
            )
        );

        FontMetrics fm =
            g.getFontMetrics();

        int width =
            fm.stringWidth(message);

        int x =
            (getWidth() - width) / 2;

        g.setColor(
            new Color(
                0,
                0,
                0,
                180
            )
        );

        g.fillRect(
            x - 15,
            45,
            width + 30,
            42
        );

        g.setColor(Color.YELLOW);

        g.drawString(
            message,
            x,
            75
        );
    }

    // =====================================================
    // SHOP MENU
    // =====================================================

    private void drawShopMenu(
        Graphics g
    ) {

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        int w = getWidth();
        int h = getHeight();

        // ---- พื้นหลังมืด ----
        g2.setColor(new Color(0, 0, 0, 200));

        g2.fillRect(0, 0, w, h);

        // ---- แผงหลัก ----
        int panelW = 960;
        int panelH = 620;
        int panelX = (w - panelW) / 2;
        int panelY = (h - panelH) / 2;

        g2.setColor(new Color(0, 0, 0, 110));

        g2.fillRoundRect(
            panelX + 8, panelY + 10, panelW, panelH, 36, 36
        );

        g2.setPaint(
            new GradientPaint(
                0, panelY, new Color(96, 62, 36),
                0, panelY + panelH, new Color(62, 38, 22)
            )
        );

        g2.fillRoundRect(panelX, panelY, panelW, panelH, 36, 36);

        g2.setColor(new Color(224, 190, 130));

        g2.setStroke(new BasicStroke(5));

        g2.drawRoundRect(panelX, panelY, panelW, panelH, 36, 36);

        g2.setColor(new Color(40, 22, 10));

        g2.setStroke(new BasicStroke(2));

        g2.drawRoundRect(
            panelX + 9, panelY + 9, panelW - 18, panelH - 18, 28, 28
        );

        // ---- หัวร้าน ----
        g2.setFont(new Font("Arial", Font.BOLD, 52));

        drawCenteredText(
            g2,
            "SHOP",
            panelX + panelW / 2,
            panelY + 78,
            new Color(255, 232, 170)
        );

        // ---- เงินของผู้เล่น ----
        String moneyText = "Money: " + player.getMoney();

        g2.setFont(new Font("Arial", Font.BOLD, 22));

        int moneyW =
            g2.getFontMetrics().stringWidth(moneyText) + 36;

        int moneyX = panelX + panelW - moneyW - 34;
        int moneyY = panelY + 34;

        g2.setColor(new Color(25, 15, 6, 220));

        g2.fillRoundRect(moneyX, moneyY, moneyW, 42, 42, 42);

        g2.setColor(new Color(255, 205, 80));

        g2.setStroke(new BasicStroke(2));

        g2.drawRoundRect(moneyX, moneyY, moneyW, 42, 42, 42);

        drawCenteredText(
            g2,
            moneyText,
            moneyX + moneyW / 2,
            moneyY + 29,
            new Color(255, 215, 90)
        );

        // ---- การ์ดสินค้า 4 ใบ ----
        int cardW = 205;
        int cardH = 380;
        int cardGap = 24;

        int cardsTotal = 4 * cardW + 3 * cardGap;
        int cardStartX = panelX + (panelW - cardsTotal) / 2;
        int cardY = panelY + 122;

        for (int i = 0; i < 4; i++) {

            int cx = cardStartX + i * (cardW + cardGap);

            boolean canAfford =
                player.getMoney() >= ITEM_PRICES[i];

            // ตัวการ์ด
            g2.setPaint(
                new GradientPaint(
                    0, cardY, new Color(240, 222, 184),
                    0, cardY + cardH, new Color(205, 176, 128)
                )
            );

            g2.fillRoundRect(cx, cardY, cardW, cardH, 26, 26);

            g2.setColor(
                canAfford
                    ? new Color(255, 205, 80)
                    : new Color(130, 100, 70)
            );

            g2.setStroke(new BasicStroke(4));

            g2.drawRoundRect(cx, cardY, cardW, cardH, 26, 26);

            // ปุ่มลัด
            g2.setColor(new Color(70, 40, 20));

            g2.fillOval(cx + 12, cardY + 12, 38, 38);

            g2.setFont(new Font("Arial", Font.BOLD, 22));

            drawCenteredText(
                g2,
                String.valueOf(i + 1),
                cx + 31,
                cardY + 40,
                new Color(255, 232, 170)
            );

            // กรอบรูปสินค้า
            g2.setColor(new Color(90, 60, 36, 70));

            g2.fillRoundRect(
                cx + 28, cardY + 38, cardW - 56, 130, 22, 22
            );

            drawImageFit(
                g2,
                itemImages[i],
                cx + 40,
                cardY + 46,
                cardW - 80,
                114,
                ITEM_NAMES[i].substring(0, 1)
            );

            // ชื่อสินค้า
            g2.setFont(new Font("Arial", Font.BOLD, 19));

            drawCenteredText(
                g2,
                ITEM_NAMES[i],
                cx + cardW / 2,
                cardY + 204,
                new Color(60, 34, 16)
            );

            // คำอธิบาย (ตัดบรรทัดอัตโนมัติ)
            g2.setFont(new Font("Arial", Font.PLAIN, 14));

            drawWrappedCenter(
                g2,
                ITEM_DESC[i],
                cx + cardW / 2,
                cardY + 232,
                cardW - 36,
                new Color(95, 60, 36)
            );

            // ราคา
            g2.setColor(
                canAfford
                    ? new Color(60, 40, 16)
                    : new Color(120, 30, 30)
            );

            g2.fillRoundRect(
                cx + 28, cardY + 282, cardW - 56, 42, 42, 42
            );

            g2.setFont(new Font("Arial", Font.BOLD, 21));

            drawCenteredText(
                g2,
                ITEM_PRICES[i] + " $",
                cx + cardW / 2,
                cardY + 311,
                canAfford
                    ? new Color(255, 215, 90)
                    : new Color(255, 170, 170)
            );

            // จำนวนที่มี
            g2.setFont(new Font("Arial", Font.BOLD, 15));

            drawCenteredText(
                g2,
                "Owned: " + getItemCount(i),
                cx + cardW / 2,
                cardY + 356,
                new Color(70, 42, 20)
            );
        }

        // ---- ข้อความท้ายแผง ----
        g2.setFont(new Font("Arial", Font.BOLD, 20));

        drawCenteredText(
            g2,
            "Press 1 - 4 to buy",
            panelX + panelW / 2,
            panelY + panelH - 52,
            new Color(255, 232, 170)
        );

        g2.setFont(new Font("Arial", Font.PLAIN, 16));

        drawCenteredText(
            g2,
            "ESC / SPACE = Close Shop",
            panelX + panelW / 2,
            panelY + panelH - 26,
            new Color(210, 185, 140)
        );
    }

    // ตัดคำขึ้นบรรทัดใหม่แล้ววางกึ่งกลาง
    private void drawWrappedCenter(
        Graphics2D g2,
        String text,
        int centerX,
        int y,
        int maxWidth,
        Color color
    ) {

        FontMetrics fm = g2.getFontMetrics();

        String[] words = text.split(" ");

        String line = "";

        int lineY = y;

        for (String word : words) {

            String test =
                line.isEmpty() ? word : line + " " + word;

            if (fm.stringWidth(test) > maxWidth && !line.isEmpty()) {

                int lw = fm.stringWidth(line);

                g2.setColor(color);

                g2.drawString(line, centerX - lw / 2, lineY);

                lineY += fm.getHeight();

                line = word;

            } else {

                line = test;
            }
        }

        if (!line.isEmpty()) {

            int lw = fm.stringWidth(line);

            g2.setColor(color);

            g2.drawString(line, centerX - lw / 2, lineY);
        }
    }

    // =====================================================
    // STYLED BUTTON (ปุ่มมนไล่สี มี hover)
    // =====================================================

    private static class StyledButton extends JButton {

        private final Color top;
        private final Color bottom;
        private boolean hover = false;

        StyledButton(String text, Color top, Color bottom) {

            super(text);

            this.top = top;
            this.bottom = bottom;

            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);

            // ไม่แย่งโฟกัสคีย์บอร์ดจากเกม
            setFocusable(false);

            setFont(new Font("Arial", Font.BOLD, 26));

            setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            );

            addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseEntered(MouseEvent e) {

                        hover = true;

                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {

                        hover = false;

                        repaint();
                    }
                }
            );
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            );

            int w = getWidth();
            int h = getHeight();

            boolean pressed = getModel().isPressed();

            Color t = hover ? top.brighter() : top;
            Color b = hover ? bottom.brighter() : bottom;

            int offset = pressed ? 2 : 0;

            // เงา
            g2.setColor(new Color(0, 0, 0, 120));

            g2.fillRoundRect(2, 5, w - 4, h - 5, h, h);

            // ตัวปุ่ม
            g2.setPaint(new GradientPaint(0, 0, t, 0, h, b));

            g2.fillRoundRect(0, offset, w - 2, h - 5, h, h);

            // ขอบ
            g2.setColor(
                new Color(255, 255, 255, hover ? 240 : 160)
            );

            g2.setStroke(new BasicStroke(3));

            g2.drawRoundRect(2, offset + 2, w - 6, h - 9, h, h);

            // ข้อความ
            g2.setFont(getFont());

            FontMetrics fm = g2.getFontMetrics();

            int tx = (w - fm.stringWidth(getText())) / 2;
            int ty = (h - 5 - fm.getHeight()) / 2 + fm.getAscent() + offset;

            g2.setColor(new Color(0, 0, 0, 140));

            g2.drawString(getText(), tx + 2, ty + 2);

            g2.setColor(Color.WHITE);

            g2.drawString(getText(), tx, ty);

            g2.dispose();
        }
    }

    // =====================================================
    // SCREEN HELPERS
    // =====================================================

    private final java.util.Map<BufferedImage, BufferedImage>
        backdropCache = new java.util.HashMap<>();

    private void enableSmooth(Graphics2D g2) {

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );
    }

    // วาดรูปคลุมทั้งพื้นที่ (ตัดส่วนเกิน) ใช้ทำพื้นหลังเบลอ
    private void drawCoverImage(
        Graphics2D g2,
        BufferedImage image,
        int w,
        int h
    ) {

        if (image == null) {

            g2.setPaint(
                new GradientPaint(
                    0, 0, new Color(20, 24, 44),
                    0, h, new Color(8, 8, 14)
                )
            );

            g2.fillRect(0, 0, w, h);

            return;
        }

        double scale =
            Math.max(
                (double) w / image.getWidth(),
                (double) h / image.getHeight()
            );

        int dw = (int) Math.ceil(image.getWidth() * scale);

        int dh = (int) Math.ceil(image.getHeight() * scale);

        g2.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        g2.drawImage(
            image,
            (w - dw) / 2,
            (h - dh) / 2,
            dw,
            dh,
            null
        );
    }

    // พื้นหลังเบลอและมืด สร้างครั้งเดียวแล้วเก็บไว้ (ไม่สร้างทุกเฟรม)
    private BufferedImage getBackdrop(
        BufferedImage source,
        int w,
        int h
    ) {

        BufferedImage cached = backdropCache.get(source);

        if (
            cached != null
            && cached.getWidth() == w
            && cached.getHeight() == h
        ) {

            return cached;
        }

        BufferedImage small =
            new BufferedImage(
                Math.max(1, w / 32),
                Math.max(1, h / 32),
                BufferedImage.TYPE_INT_RGB
            );

        Graphics2D sg = small.createGraphics();

        drawCoverImage(sg, source, small.getWidth(), small.getHeight());

        sg.dispose();

        BufferedImage out =
            new BufferedImage(
                w,
                h,
                BufferedImage.TYPE_INT_RGB
            );

        Graphics2D og = out.createGraphics();

        og.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        og.drawImage(small, 0, 0, w, h, null);

        og.setColor(new Color(0, 0, 0, 150));

        og.fillRect(0, 0, w, h);

        og.dispose();

        backdropCache.put(source, out);

        return out;
    }

    // วาดรูปแบบเห็นครบทั้งรูป (ไม่ตัด) กึ่งกลางจอ ส่วนที่เหลือเป็นพื้นหลังเบลอ
    // คืนกรอบที่รูปถูกวาด เอาไว้วางปุ่ม/ข้อความให้ตรงกับรูป
    private Rectangle drawArtImage(
        Graphics2D g2,
        BufferedImage image,
        int w,
        int h
    ) {

        g2.drawImage(getBackdrop(image, w, h), 0, 0, null);

        if (image == null) {

            return new Rectangle(0, 0, w, h);
        }

        double scale =
            Math.min(
                (double) w / image.getWidth(),
                (double) h / image.getHeight()
            );

        int dw = (int) Math.round(image.getWidth() * scale);

        int dh = (int) Math.round(image.getHeight() * scale);

        int dx = (w - dw) / 2;

        int dy = (h - dh) / 2;

        g2.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        g2.drawImage(image, dx, dy, dw, dh, null);

        return new Rectangle(dx, dy, dw, dh);
    }

    private void drawGlassPanel(
        Graphics2D g2,
        int x,
        int y,
        int w,
        int h,
        Color border
    ) {

        g2.setColor(new Color(0, 0, 0, 110));

        g2.fillRoundRect(x + 5, y + 8, w, h, 40, 40);

        g2.setColor(new Color(10, 10, 18, 200));

        g2.fillRoundRect(x, y, w, h, 40, 40);

        g2.setColor(border);

        g2.setStroke(new BasicStroke(3));

        g2.drawRoundRect(x, y, w, h, 40, 40);
    }

    private Font chipFont(int h) {

        return new Font(
            "Arial",
            Font.BOLD,
            Math.round(h * 0.45f)
        );
    }

    private int chipWidth(
        Graphics2D g2,
        String text,
        int h
    ) {

        return Math.max(
            h,
            g2.getFontMetrics(chipFont(h)).stringWidth(text) + 22
        );
    }

    // ปุ่มคีย์บอร์ดจำลอง คืนความกว้างที่วาด
    private int drawKeyChip(
        Graphics2D g2,
        String text,
        int x,
        int y,
        int h
    ) {

        int w = chipWidth(g2, text, h);

        g2.setFont(chipFont(h));

        g2.setColor(new Color(0, 0, 0, 120));

        g2.fillRoundRect(x + 2, y + 3, w, h, 12, 12);

        g2.setPaint(
            new GradientPaint(
                0, y, new Color(250, 242, 220),
                0, y + h, new Color(214, 196, 150)
            )
        );

        g2.fillRoundRect(x, y, w, h, 12, 12);

        g2.setColor(new Color(110, 80, 40));

        g2.setStroke(new BasicStroke(2));

        g2.drawRoundRect(x, y, w, h, 12, 12);

        FontMetrics fm = g2.getFontMetrics();

        g2.setColor(new Color(70, 45, 20));

        g2.drawString(
            text,
            x + (w - fm.stringWidth(text)) / 2,
            y + (h - fm.getHeight()) / 2 + fm.getAscent()
        );

        return w;
    }

    private String formatTime(long millis) {

        long total = Math.max(0, millis / 1000);

        return String.format(
            "%d:%02d",
            total / 60,
            total % 60
        );
    }

    // =====================================================
    // START SCREEN
    // =====================================================

    private void drawStartScreen(
        Graphics g
    ) {

        Graphics2D g2 = (Graphics2D) g;

        enableSmooth(g2);

        int w = getWidth();
        int h = getHeight();

        // รูปมีชื่อเกมอยู่แล้ว วาดเต็มรูปแล้วใช้พื้นถนนด้านล่างวางปุ่ม
        Rectangle art =
            drawArtImage(g2, startPageImage, w, h);

        int roadTop =
            art.y + (int) (art.height * 0.84);

        // เฟดมืดที่พื้นถนนให้อ่านข้อความง่าย
        g2.setPaint(
            new GradientPaint(
                0, roadTop - 50, new Color(0, 0, 0, 0),
                0, art.y + art.height, new Color(0, 0, 0, 170)
            )
        );

        g2.fillRect(
            art.x,
            roadTop - 50,
            art.width,
            art.y + art.height - (roadTop - 50)
        );

        // ---- ปุ่ม START ----
        int bw = 320;
        int bh = 64;

        int bx = (w - bw) / 2;
        int by = roadTop + 12;

        if (
            startButton.getX() != bx
            || startButton.getY() != by
            || startButton.getWidth() != bw
            || startButton.getHeight() != bh
        ) {

            startButton.setBounds(bx, by, bw, bh);
        }

        // ---- แถวปุ่มควบคุม ----
        int chipH = 34;

        int rowY = by + bh + 24;

        String[][] groups = {
            {"W", "A", "S", "D"},
            {"SPACE"},
            {"1-4"},
            {"ENTER"}
        };

        String[] labels = {
            "Move",
            "Interact",
            "Use Item",
            "Start"
        };

        Font labelFont =
            new Font("Arial", Font.BOLD, 17);

        int chipGap = 6;
        int labelGap = 12;
        int groupGap = 46;

        int[] groupWidth = new int[groups.length];

        int total = 0;

        for (int i = 0; i < groups.length; i++) {

            int gw = 0;

            for (int k = 0; k < groups[i].length; k++) {

                gw += chipWidth(g2, groups[i][k], chipH);

                if (k > 0) {

                    gw += chipGap;
                }
            }

            gw +=
                labelGap
                + g2.getFontMetrics(labelFont)
                    .stringWidth(labels[i]);

            groupWidth[i] = gw;

            total += gw;
        }

        total += groupGap * (groups.length - 1);

        int x = (w - total) / 2;

        for (int i = 0; i < groups.length; i++) {

            for (int k = 0; k < groups[i].length; k++) {

                x +=
                    drawKeyChip(
                        g2,
                        groups[i][k],
                        x,
                        rowY,
                        chipH
                    )
                    + chipGap;
            }

            x += labelGap - chipGap;

            g2.setFont(labelFont);

            drawShadowText(
                g2,
                labels[i],
                x,
                rowY + chipH / 2 + 6,
                new Color(235, 235, 245)
            );

            x +=
                g2.getFontMetrics().stringWidth(labels[i])
                + groupGap;
        }
    }

    // =====================================================
    // GAME OVER
    // =====================================================

    private void drawGameOver(
        Graphics g
    ) {

        Graphics2D g2 = (Graphics2D) g;

        enableSmooth(g2);

        int w = getWidth();
        int h = getHeight();

        // รูปมีข้อความ "CAUGHT! GAME OVER." อยู่แล้ว จึงไม่วาดซ้ำ
        Rectangle art =
            drawArtImage(g2, gameOverImage, w, h);

        // ---- แถบสรุปด้านล่าง ----
        int barW = Math.min(w - 80, 1400);
        int barH = 100;

        int barX = (w - barW) / 2;
        int barY = h - barH - 20;

        drawGlassPanel(
            g2,
            barX,
            barY,
            barW,
            barH,
            new Color(235, 80, 80)
        );

        // ---- คะแนน ----
        int scoreCx = barX + 36 + 170;

        g2.setFont(new Font("Arial", Font.BOLD, 15));

        drawCenteredText(
            g2,
            "F I N A L   S C O R E",
            scoreCx,
            barY + 32,
            new Color(255, 225, 140)
        );

        g2.setFont(new Font("Arial", Font.BOLD, 50));

        drawCenteredText(
            g2,
            String.format("%,d", player.getScore()),
            scoreCx,
            barY + 80,
            new Color(255, 215, 0)
        );

        // ---- เส้นคั่น ----
        g2.setColor(new Color(255, 255, 255, 60));

        g2.fillRect(barX + 36 + 340 + 20, barY + 20, 2, barH - 40);

        // ---- สถิติ ----
        long survived =
            gameEndTime > 0
                ? gameEndTime - startTime
                : System.currentTimeMillis() - startTime;

        int statX = barX + 36 + 340 + 50;

        g2.setFont(new Font("Arial", Font.PLAIN, 22));

        drawShadowText(
            g2,
            "Survived   " + formatTime(survived),
            statX,
            barY + 42,
            new Color(225, 225, 235)
        );

        drawShadowText(
            g2,
            "Money   " + player.getMoney(),
            statX,
            barY + 78,
            new Color(255, 215, 90)
        );

        // ---- ปุ่ม ----
        int bh = 56;

        int by = barY + (barH - bh) / 2;

        int exitW = 170;
        int restartW = 230;

        int exitX = barX + barW - 36 - exitW;
        int restartX = exitX - 20 - restartW;

        if (
            restartButton.getX() != restartX
            || restartButton.getY() != by
            || restartButton.getWidth() != restartW
            || restartButton.getHeight() != bh
        ) {

            restartButton.setBounds(restartX, by, restartW, bh);

            exitButton.setBounds(exitX, by, exitW, bh);
        }
    }

    // =====================================================
    // KEY HELPERS
    // =====================================================

    private int getNumberKey(
        int key
    ) {

        if (
            key == KeyEvent.VK_1
            ||
            key == KeyEvent.VK_NUMPAD1
        ) {

            return 1;
        }

        if (
            key == KeyEvent.VK_2
            ||
            key == KeyEvent.VK_NUMPAD2
        ) {

            return 2;
        }

        if (
            key == KeyEvent.VK_3
            ||
            key == KeyEvent.VK_NUMPAD3
        ) {

            return 3;
        }

        if (
            key == KeyEvent.VK_4
            ||
            key == KeyEvent.VK_NUMPAD4
        ) {

            return 4;
        }

        return 0;
    }

    // =====================================================
    // KEY PRESSED
    // =====================================================

    @Override
    public void keyPressed(
        KeyEvent e
    ) {

        // ENTER / SPACE = เริ่มเกม (หน้าเริ่ม), ENTER = เล่นใหม่ (หน้า Game Over)
        if (gameState == GameState.START) {

            if (
                e.getKeyCode() == KeyEvent.VK_ENTER
                ||
                e.getKeyCode() == KeyEvent.VK_SPACE
            ) {

                startGame();
            }

            return;
        }

        if (gameState == GameState.GAME_OVER) {

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                startGame();
            }

            return;
        }

        if (
            gameState !=
            GameState.PLAYING
        ) {

            return;
        }

        int key =
            e.getKeyCode();

        // true เฉพาะครั้งแรกที่กด
        boolean firstPress =
            heldKeys.add(key);

        // =================================================
        // SHOP MENU
        // =================================================

        if (shopOpen) {

            if (!firstPress) {

                return;
            }

            int number =
                getNumberKey(key);

            if (number != 0) {

                buyItem(number);
            }

            if (
                key ==
                KeyEvent.VK_ESCAPE
                ||
                key ==
                KeyEvent.VK_SPACE
            ) {

                shopOpen = false;

                requestFocusInWindow();
            }

            return;
        }

        // =================================================
        // MOVEMENT
        // =================================================

        if (
            key ==
            KeyEvent.VK_W
        ) {

            up = true;
        }

        if (
            key ==
            KeyEvent.VK_S
        ) {

            down = true;
        }

        if (
            key ==
            KeyEvent.VK_A
        ) {

            left = true;
        }

        if (
            key ==
            KeyEvent.VK_D
        ) {

            right = true;
        }

        // =================================================
        // FIRST PRESS ONLY
        // =================================================

        if (!firstPress) {

            return;
        }

        // =================================================
        // SPACE
        // =================================================

        if (
            key ==
            KeyEvent.VK_SPACE
        ) {

            spacePressed = true;

            return;
        }

        // =================================================
        // USE ITEM
        // =================================================

        int number =
            getNumberKey(key);

        if (number != 0) {

            useItem(number);
        }
    }

    // =====================================================
    // KEY RELEASED
    // =====================================================

    @Override
    public void keyReleased(
        KeyEvent e
    ) {

        int key =
            e.getKeyCode();

        heldKeys.remove(key);

        if (
            key ==
            KeyEvent.VK_W
        ) {

            up = false;
        }

        if (
            key ==
            KeyEvent.VK_S
        ) {

            down = false;
        }

        if (
            key ==
            KeyEvent.VK_A
        ) {

            left = false;
        }

        if (
            key ==
            KeyEvent.VK_D
        ) {

            right = false;
        }
    }

    // =====================================================
    // KEY TYPED
    // =====================================================

    @Override
    public void keyTyped(
        KeyEvent e
    ) {
    }
}
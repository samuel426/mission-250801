package org.example;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class App {
    private final Scanner sc = new Scanner(System.in);
    private final List<WiseSaying> list = new ArrayList<>();

    private final Path DB_DIR = Paths.get("db", "wiseSaying");
    private final Path LAST_ID_FILE = DB_DIR.resolve("lastId.txt");

    private int lastId = 0;

    public void run() {
        JsonExample.ensureDir(DB_DIR);
        loadFromFiles();

        System.out.println("== 명언 앱 ==");
        while (true) {
            System.out.print("명령) ");
            String cmd = sc.nextLine().trim();

            if (cmd.equals("종료")) break;
            else if (cmd.equals("등록")) handleAdd();
            else if (cmd.equals("목록")) handleList();
            else if (cmd.startsWith("삭제")) handleDelete(cmd);
            else if (cmd.startsWith("수정")) handleUpdate(cmd);
            else if (cmd.equals("빌드")) handleBuild();
            else if (cmd.isBlank()) continue;
            else System.out.println("알 수 없는 명령입니다.");
        }
    }

    private void handleAdd() {
        System.out.print("명언 : ");
        String content = sc.nextLine();
        System.out.print("작가 : ");
        String author = sc.nextLine();

        int id = ++lastId;
        WiseSaying ws = new WiseSaying(id, content, author);
        list.add(ws);

        saveWiseSaying(ws);
        saveLastId();

        System.out.printf("%d번 명언이 등록되었습니다.%n", id);
    }

    private void handleList() {
        System.out.println("번호 / 작가 / 명언");
        System.out.println("----------------------");

        list.stream()
                .sorted(Comparator.comparingInt(WiseSaying::getId).reversed())
                .forEach(ws -> System.out.printf("%d / %s / %s%n",
                        ws.getId(), ws.getAuthor(), ws.getContent()));
    }

    private void handleDelete(String cmd) {
        Integer id = parseId(cmd);
        if (id == null) {
            System.out.println("형식 : 삭제?id=숫자");
            return;
        }
        WiseSaying target = findById(id);
        if (target == null) {
            System.out.printf("%d번 명언은 존재하지 않습니다.%n", id);
            return;
        }
        list.remove(target);

        try {
            Files.deleteIfExists(DB_DIR.resolve(id + ".json"));
        } catch (IOException ignored) {}
        System.out.printf("%d번 명언이 삭제되었습니다.%n", id);
    }


    private void handleUpdate(String cmd) {
        Integer id = parseId(cmd);
        if (id == null) {
            System.out.println("형식 : 삭제?id=숫자");
            return;
        }
        WiseSaying target = findById(id);
        if (target == null) {
            System.out.printf("%d번 명언은 존재하지 않습니다.%n", id);
            return;
        }

        System.out.printf("명언(기존) : %s%n", target.getContent());
        System.out.print("명언 : ");
        String newContent = sc.nextLine();
        if (!newContent.isBlank()) target.setContent(newContent);

        System.out.printf("작가(기존) : %s%n", target.getAuthor());
        System.out.print("작가 : ");
        String newAuthor = sc.nextLine();
        if (!newAuthor.isBlank()) target.setAuthor(newAuthor);

        saveWiseSaying(target);
    }

    private void handleBuild() {
        Path out = Paths.get("data.json");
        List<WiseSaying> sorted = new ArrayList<>(list);
        sorted.sort(Comparator.comparingInt(WiseSaying::getId));
        JsonExample.writeDataJson(out, sorted);
        System.out.println("data.json 파일의 내용이 갱신되었습니다.");
    }

    private void loadFromFiles() {

        lastId = JsonExample.readLastId(LAST_ID_FILE);

        list.clear();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(DB_DIR, "*.json")) {
            for (Path p : ds) {
                try {
                    WiseSaying ws = JsonExample.readWiseSaying(p);
                    list.add(ws);

                    if (ws.getId() > lastId) lastId = ws.getId();
                } catch (RuntimeException ignored) {

                }
            }
        } catch (IOException ignored) {}

        saveLastId();
    }

    private void saveWiseSaying(WiseSaying ws) {
        Path file = DB_DIR.resolve(ws.getId() + ".json");
        JsonExample.writeWiseSaying(file, ws);
    }

    private void saveLastId() {
        JsonExample.writeLastId(LAST_ID_FILE, lastId);
    }

    private WiseSaying findById(int id) {
        for (WiseSaying ws : list) if (ws.getId() == id) return ws;
        return null;
    }

    private Integer parseId(String cmd) {
        int q = cmd.indexOf('?');
        if (q < 0) return null;
        String qs = cmd.substring(q +1);
        String[] kv = qs.split("=", 2);
        if (kv.length != 2) return null;
        if (!kv[0].trim().equals("id")) return null;
        try {
            return Integer.parseInt(kv[1].trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
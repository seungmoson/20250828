import java.util.*;

public class BaseballCom {
    static Scanner scanner = new Scanner(System.in);
    static Random random = new Random();

    static String[] teams = {"사용자(A)", "컴퓨터(B)"};
    static int[] scores = {0, 0}; // 사용자, 컴퓨터 점수

    public static void main(String[] args) {
        System.out.println("=== 숫자 야구 ⚾ 사용자 vs 컴퓨터 (공격 10회 제한, 중복 추측 제외) ===");

        // 9이닝 경기
        for (int inning = 1; inning <= 9; inning++) {
            System.out.println("\n===== " + inning + " 이닝 =====");

            // 사용자 공격, 컴퓨터 수비
            playHalfInningUserAttack(0, 1);

            // 컴퓨터 공격, 사용자 수비
            playHalfInningComputerAttack(1, 0);

            System.out.println("현재 스코어: 사용자 " + scores[0] + " - " + scores[1] + " 컴퓨터");
        }

        // 최종 결과
        System.out.println("\n===== 경기 종료! =====");
        System.out.println("최종 스코어: 사용자 " + scores[0] + " - " + scores[1] + " 컴퓨터");

        if (scores[0] > scores[1]) {
            System.out.println("🎉 사용자의 승리!");
        } else if (scores[0] < scores[1]) {
            System.out.println("😢 컴퓨터의 승리!");
        } else {
            System.out.println("무승부!");
        }
    }

    // 사용자 공격 턴
    static void playHalfInningUserAttack(int attackTeam, int defenseTeam) {
        System.out.println("▶ 사용자 공격, 컴퓨터 수비");

        List<Integer> defense = generateDefense();
        int outs = 0;
        int attempts = 0; // 공격 횟수 제한

        while (outs < 3 && attempts < 10) {
            System.out.print("세 자리 숫자를 입력하세요 (중복X): ");
            String guess = scanner.next();
            attempts++;

            if (!isValidGuess(guess)) {
                System.out.println("잘못된 입력입니다. 세 자리 중복 없는 숫자를 입력하세요.");
                continue;
            }

            List<Integer> attack = new ArrayList<>();
            for (char c : guess.toCharArray()) {
                attack.add(c - '0');
            }

            int[] result = judge(attack, defense);
            int strikes = result[0], balls = result[1];

            if (strikes == 0 && balls == 0) {
                outs++;
                System.out.println("Out! (" + outs + " 아웃)");
            } else {
                System.out.println(strikes + "S " + balls + "B");
            }

            if (strikes == 3) {
                scores[attackTeam]++;
                System.out.println("사용자 득점, 현재 점수 : " + scores[0]);
                return; // 이닝 종료
            }
        }

        if (outs == 3) {
            System.out.println("⚾ 3아웃! 공수 교대");
        } else if (attempts == 10) {
            System.out.println("⏳ 공격 횟수(10번) 초과! 득점 실패, 공수 교대");
            System.out.println("정답은 " + defense + "입니다");

        }
    }

    // 컴퓨터 공격 턴 (지능적 추측)
    static void playHalfInningComputerAttack(int attackTeam, int defenseTeam) {
        System.out.println("▶ 컴퓨터 공격, 사용자 수비");

        // ✅ 사용자가 수비 숫자 입력
        List<Integer> defense = null;
        while (defense == null) {
            System.out.print("사용자(수비), 세 자리 숫자를 입력하세요 (중복X, 컴퓨터에게는 보이지 않음): ");
            String input = scanner.next();

            if (!isValidGuess(input)) {
                System.out.println("잘못된 입력입니다. 세 자리 중복 없는 숫자를 입력하세요.");
                continue;
            }

            defense = new ArrayList<>();
            for (char c : input.toCharArray()) {
                defense.add(c - '0');
            }
        }

        int outs = 0;
        int attempts = 0;

        // ✅ 모든 가능한 후보 생성
        List<List<Integer>> candidates = generateAllCandidates();
        Random rand = new Random();

        while (outs < 3 && attempts < 10 && !candidates.isEmpty()) {
            // 후보군에서 랜덤 뽑기
            List<Integer> attack = candidates.get(rand.nextInt(candidates.size()));
            attempts++;

            System.out.println("컴퓨터의 추측: " + attack);

            int[] result = judge(attack, defense);
            int strikes = result[0], balls = result[1];

            if (strikes == 0 && balls == 0) {
                outs++;
                System.out.println("Out! (" + outs + " 아웃)");
            } else {
                System.out.println(strikes + "S " + balls + "B");
            }

            // ✅ 똑똑함 조정 (70% 확률로만 필터링 반영)
            if (rand.nextDouble() < 0.7) {
                candidates.removeIf(c -> !matchesResult(attack, c, strikes, balls));
            }

            if (strikes == 3) {
                scores[attackTeam]++;
                System.out.println("😱 컴퓨터 득점! 현재 점수: " + scores[1]);
                return;
            }
        }


        if (outs == 3) {
            System.out.println("⚾ 3아웃! 공수 교대");
        } else if (attempts == 10) {
            System.out.println("⏳ 공격 횟수(10번) 초과! 득점 실패, 공수 교대");
        }
    }

    // 모든 3자리 후보 (0~9, 중복 없음)
    static List<List<Integer>> generateAllCandidates() {
        List<List<Integer>> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (j == i) continue;
                for (int k = 0; k < 10; k++) {
                    if (k == i || k == j) continue;
                    list.add(Arrays.asList(i, j, k));
                }
            }
        }
        return list;
    }

    // 두 숫자 리스트 비교 → 특정 판정과 일치하는지 확인
    static boolean matchesResult(List<Integer> attack, List<Integer> candidate, int strikes, int balls) {
        int[] result = judge(candidate, attack);
        return result[0] == strikes && result[1] == balls;
    }


    // 판정 (strike, ball 반환)
    static int[] judge(List<Integer> attack, List<Integer> defense) {
        int strikes = 0, balls = 0;
        for (int i = 0; i < 3; i++) {
            if (attack.get(i).equals(defense.get(i))) {
                strikes++;
            } else if (defense.contains(attack.get(i))) {
                balls++;
            }
        }
        return new int[]{strikes, balls};
    }

    // 랜덤 세 자리 숫자 생성 (중복 없음)
    static List<Integer> generateDefense() {
        List<Integer> numbers = new ArrayList<>();
        while (numbers.size() < 3) {
            int n = random.nextInt(10);
            if (!numbers.contains(n)) numbers.add(n);
        }
        return numbers;
    }

    // 입력 검증
    static boolean isValidGuess(String guess) {
        if (guess.length() != 3) return false;
        Set<Character> set = new HashSet<>();
        for (char c : guess.toCharArray()) {
            if (!Character.isDigit(c)) return false;
            set.add(c);
        }
        return set.size() == 3;
    }
}

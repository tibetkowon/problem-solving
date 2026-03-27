-- ── 단원 (Chapter) ──────────────────────────────────────────────
INSERT INTO chapter (id, name) VALUES (1, '자료구조');
INSERT INTO chapter (id, name) VALUES (2, '알고리즘');
INSERT INTO chapter (id, name) VALUES (3, '운영체제');

-- ── 문제 (Problem) ───────────────────────────────────────────────
-- 자료구조 - 객관식(단일)
INSERT INTO problem (id, chapter_id, content, type, explanation, answer)
VALUES (1, 1, 'LIFO(Last In First Out) 방식으로 동작하는 자료구조는?', 'OBJECTIVE_SINGLE', '스택(Stack)은 LIFO 방식으로, 나중에 삽입된 데이터가 먼저 삭제됩니다.', NULL);

-- 자료구조 - 객관식(복수)
INSERT INTO problem (id, chapter_id, content, type, explanation, answer)
VALUES (2, 1, '선형 자료구조에 해당하는 것을 모두 고르시오.', 'OBJECTIVE_MULTIPLE', '배열, 연결 리스트, 스택, 큐는 선형 자료구조이고 트리, 그래프는 비선형 자료구조입니다.', NULL);

-- 자료구조 - 주관식
INSERT INTO problem (id, chapter_id, content, type, explanation, answer)
VALUES (3, 1, 'FIFO 방식으로 동작하며 운영체제의 프로세스 스케줄링에 사용되는 자료구조의 이름을 쓰시오.', 'SUBJECTIVE', '큐(Queue)는 FIFO(First In First Out) 방식으로 동작합니다.', '큐');

-- 알고리즘 - 객관식(단일)
INSERT INTO problem (id, chapter_id, content, type, explanation, answer)
VALUES (4, 2, '정렬 알고리즘 중 평균 시간 복잡도가 O(n log n)인 것은?', 'OBJECTIVE_SINGLE', '퀵 정렬의 평균 시간 복잡도는 O(n log n)입니다. 최악의 경우 O(n²)가 될 수 있습니다.', NULL);

-- 알고리즘 - 주관식
INSERT INTO problem (id, chapter_id, content, type, explanation, answer)
VALUES (5, 2, '이진 탐색(Binary Search)이 동작하기 위한 전제 조건을 서술하시오.', 'SUBJECTIVE', '이진 탐색은 데이터가 정렬되어 있어야 동작합니다.', '정렬');

-- 운영체제 - 객관식(단일)
INSERT INTO problem (id, chapter_id, content, type, explanation, answer)
VALUES (6, 3, '프로세스와 스레드에 대한 설명으로 옳은 것은?', 'OBJECTIVE_SINGLE', '스레드는 프로세스 내의 실행 단위이며, 같은 프로세스 내 스레드들은 메모리를 공유합니다.', NULL);

-- ── 선택지 (Choice) ──────────────────────────────────────────────
-- 문제 1 선택지 (단일 정답: 스택)
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (1, 1, '스택', TRUE);
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (2, 1, '큐', FALSE);
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (3, 1, '덱', FALSE);
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (4, 1, '힙', FALSE);
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (5, 1, '트리', FALSE);

-- 문제 2 선택지 (복수 정답: 배열, 연결 리스트, 스택, 큐)
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (6, 2, '배열', TRUE);
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (7, 2, '연결 리스트', TRUE);
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (8, 2, '트리', FALSE);
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (9, 2, '스택', TRUE);
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (10, 2, '큐', TRUE);

-- 문제 4 선택지 (단일 정답: 퀵 정렬)
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (11, 4, '버블 정렬', FALSE);
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (12, 4, '삽입 정렬', FALSE);
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (13, 4, '선택 정렬', FALSE);
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (14, 4, '퀵 정렬', TRUE);
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (15, 4, '계수 정렬', FALSE);

-- 문제 6 선택지 (단일 정답: 스레드는 프로세스 내 메모리 공유)
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (16, 6, '프로세스는 스레드보다 가벼운 실행 단위이다', FALSE);
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (17, 6, '스레드는 독립적인 메모리 공간을 가진다', FALSE);
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (18, 6, '같은 프로세스의 스레드들은 힙 영역을 공유한다', TRUE);
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (19, 6, '멀티 프로세스가 멀티 스레드보다 항상 빠르다', FALSE);
INSERT INTO choice (id, problem_id, content, is_correct) VALUES (20, 6, '스레드 간 컨텍스트 스위칭 비용은 프로세스보다 크다', FALSE);

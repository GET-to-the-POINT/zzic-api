---
applyTo: '**/*.java'
---

애플리케이션 테스트는 내가 할거야 너는 테스트코드와 빌드만 신경써줘!!
사용하지 않는 메서드나 임포트, 변수는 제거한다.
최대한 절제된 코드를 작성한다.
ddd 구조를 따른다.
주석은 작성하지 않는다.
스웨거는 작성한다.
자바21 이상의 문법을 쓴다.
구 문법은 쓰지 않는다.
코드량을 줄일 수 있는 라이브러리가 있다면 활용한다. 단 활발한 업데이트가 있는것만쓴다.(마이너한 라이브러리는 쓰지 않는다. 절때로. 메이저급 라이브러리만 추가)
코드를 복잡하게 나누지 않는다. 최대한 단순하게 만든다. 단 구조가 잘못된 경우는 알아서 판단한다.
서비스는 에러를 던진다면 공통 익셉션을 만들어서 던지고, 글로벌익셉션이 그걸 적절히 처리한다.
똑같은 패키지를 2개이상 임포트할 경우 * 로 대체한다.
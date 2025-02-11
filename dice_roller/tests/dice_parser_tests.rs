use dice_parser::{parse, DiceRoll, Exploding, Expr, KeepDrop};

#[test]
fn test_basic_dice_parsing() {
    let (_, rolls) = parse("1d20").unwrap();
    assert_eq!(
        rolls,
        vec![DiceRoll::new(1, 20, Exploding::None, KeepDrop::None)]
    );
}

#[test]
fn test_multiple_dice_expressions() {
    let (expr, rolls) = parse("2d6+3").unwrap();
    println!("Expression: {:?}", expr);
    assert_eq!(
        rolls,
        vec![DiceRoll::new(2, 6, Exploding::None, KeepDrop::None),]
    );
}

#[test]
fn test_indefinite_dice() {
    let (_, rolls) = parse("4d8!!").unwrap();
    assert_eq!(
        rolls,
        vec![DiceRoll::new(4, 8, Exploding::Indefinite, KeepDrop::None)]
    );
}

#[test]
fn test_exploding_dice() {
    let (_, rolls) = parse("4d8!").unwrap();
    assert_eq!(
        rolls,
        vec![DiceRoll::new(4, 8, Exploding::Once, KeepDrop::None,)]
    );
}

#[test]
fn test_keep_highest() {
    let (_, rolls) = parse("1d10kh2").unwrap();
    assert_eq!(
        rolls,
        vec![DiceRoll::new(
            1,
            10,
            Exploding::None,
            KeepDrop::KeepHighest(2),
        )]
    );
}

#[test]
fn test_complex_expression() {
    let (expr, rolls) = parse("3d12dh1-5+2d4+1").unwrap();

    assert_eq!(
        rolls,
        vec![
            DiceRoll::new(3, 12, Exploding::None, KeepDrop::DropHighest(1)),
            DiceRoll::new(2, 4, Exploding::None, KeepDrop::None)
        ]
    );

    let expected = Expr::BinaryOp(
        Box::new(Expr::BinaryOp(
            Box::new(Expr::BinaryOp(
                Box::new(Expr::DiceRoll(DiceRoll::new(
                    3,
                    12,
                    Exploding::None,
                    KeepDrop::DropHighest(1),
                ))),
                "-".to_string(),
                Box::new(Expr::Number(5)),
            )),
            "+".to_string(),
            Box::new(Expr::DiceRoll(DiceRoll::new(
                2,
                4,
                Exploding::None,
                KeepDrop::None,
            ))),
        )),
        "+".to_string(),
        Box::new(Expr::Number(1)),
    );
    assert_eq!(expr, expected);
}

#[test]
fn test_complex_expression2() {
    let (_expr, _rolls) = parse("2d20kh1 + 3d6!! - floor(4d10) * 5d8r2").unwrap();
}

#[test]
fn test_invalid_dice_expressions() {
    let invalid_cases = vec![
        "", // Empty string
        // "20",     // Missing "d" //this is valid, it's just a constant 20
        "d",        // No dice size
        "d%",       // Invalid dice size
        "2dX",      // Non-numeric dice size
        "2dd20",    // Extra "d"
        "1d10kh",   // Missing value for keep/drop
        "1d10+kh2", // Modifier without a valid term
    ];

    for input in invalid_cases {
        let result = parse(input);
        assert!(result.is_err(), "Expected failure for '{}'", input);
    }
}

use dice_parser::{parse, DiceRoll, Exploding, KeepDrop, Expr};

#[test]
fn test_basic_dice_parsing() {
    let (_, rolls) = parse("1d20").unwrap();
    assert_eq!(rolls, vec![
        DiceRoll::new(1, 20, Exploding::None, KeepDrop::None, 0)
    ]);
}

#[test]
fn test_multiple_dice_expressions() {
    let (_, rolls) = parse("2d6+3").unwrap();
    assert_eq!(rolls, vec![
        DiceRoll::new(2, 6, Exploding::None, KeepDrop::None, 3)
    ]);
}

#[test]
fn test_exploding_dice() {
    let (_, rolls) = parse("4d8!!").unwrap();
    assert_eq!(rolls, vec![
        DiceRoll::new(4, 8, Exploding::Indefinite, KeepDrop::None, 0)
    ]);
}

#[test]
fn test_keep_highest() {
    let (_, rolls) = parse("1d10kh2+1d6+3").unwrap();
    assert_eq!(rolls, vec![
        DiceRoll::new(1, 10, Exploding::None, KeepDrop::KeepHighest(2), 0),
        DiceRoll::new(1, 6, Exploding::None, KeepDrop::None, 3)
    ]);
}

#[test]
fn test_complex_expression() {
    let (expr, rolls) = parse("3d12dh1-5+2d4+1").unwrap();

    assert_eq!(rolls, vec![
        DiceRoll::new(3, 12, Exploding::None, KeepDrop::DropHighest(1), -5),
        DiceRoll::new(2, 4, Exploding::None, KeepDrop::None, 1)
    ]);

    // Ensure correct binary operation parsing
    if let Expr::BinaryOp(left, op, right) = expr {
        assert_eq!(op, "+");
        assert!(matches!(*left, Expr::BinaryOp(_, _, _))); // 3d12dh1-5 should be inside a BinaryOp
        assert!(matches!(*right, Expr::DiceRoll(_))); // 2d4+1 should be a DiceRoll
    } else {
        panic!("Expression did not parse as expected");
    }
}

#[test]
fn test_invalid_dice_expressions() {
    let invalid_cases = vec![
        "",       // Empty string
        "20",     // Missing "d"
        "d",      // No dice size
        "d%",     // Invalid dice size
        "2dX",    // Non-numeric dice size
        "2dd20",  // Extra "d"
        "1d10kh", // Missing value for keep/drop
        "1d10+kh2", // Modifier without a valid term
    ];

    for input in invalid_cases {
        let result = parse(input);
        assert!(result.is_err(), "Expected failure for '{}'", input);
    }
}

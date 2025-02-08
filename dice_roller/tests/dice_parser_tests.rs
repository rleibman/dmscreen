use dice_parser::{parse_dice, DiceRoll, Exploding, KeepDrop};

#[test]
fn test_valid_dice_expressions() {
    let cases = vec![
        ("d20", DiceRoll { num_dice: 1, die_size: 20, exploding: Exploding::None, keep_drop: KeepDrop::None, modifier: 0 }),
        ("2d6", DiceRoll { num_dice: 2, die_size: 6, exploding: Exploding::None, keep_drop: KeepDrop::None, modifier: 0 }),
        ("4d8!", DiceRoll { num_dice: 4, die_size: 8, exploding: Exploding::Once, keep_drop: KeepDrop::None, modifier: 0 }),
        ("1d10!!kh2+3", DiceRoll { num_dice: 1, die_size: 10, exploding: Exploding::Indefinite, keep_drop: KeepDrop::KeepHighest(2), modifier: 3 }),
        ("3d12dh1-5", DiceRoll { num_dice: 3, die_size: 12, exploding: Exploding::None, keep_drop: KeepDrop::DropHighest(1), modifier: -5 }),
    ];

    for (input, expected) in cases {
        let result = parse_dice(input);
        assert!(result.is_ok(), "Parsing failed for '{}'", input);
        assert_eq!(result.unwrap(), expected, "Mismatch for '{}'", input);
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
        "1d10+X", // Invalid modifier
    ];

    for input in invalid_cases {
        let result = parse_dice(input);
        assert!(result.is_err(), "Expected failure for '{}'", input);
    }
}

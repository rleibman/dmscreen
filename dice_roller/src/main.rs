use dice_parser::parse_dice;

fn main() {
    let test_rolls = ["d20", "2d6", "4d8!", "1d10!!kh2+3", "3d12dh1-5"];

    for roll in &test_rolls {
        println!("Parsing: {}", roll);
        match parse_dice(roll) {
            Ok(dice_roll) => println!("Parsed: {:?}", dice_roll),
            Err(err) => println!("Error: {}", err),
        }
    }
}

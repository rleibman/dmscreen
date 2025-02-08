use pest::Parser;
use pest_derive::Parser;

mod dice;
pub use dice::{DiceRoll, Exploding, KeepDrop};

#[derive(Parser)]
#[grammar = "dice.pest"]
pub struct DiceParser;

pub fn parse_dice(input: &str) -> Result<DiceRoll, String> {
    let mut num_dice = 1; // Default to 1
    let mut die_size = 0;
    let mut exploding = Exploding::None;
    let mut keep_drop = KeepDrop::None;
    let mut modifier = 0;

    let pairs = DiceParser::parse(Rule::dice_expr, input)
        .map_err(|e| format!("Parsing error: {}", e))?
        .next() // Get the first (and only) `dice_expr` pair
        .ok_or_else(|| "Empty input".to_string())?
        .into_inner(); // Get nested pairs inside `dice_expr`

    for pair in pairs {
        match pair.as_rule() {
            Rule::num_dice => {
                num_dice = pair.as_str().parse().unwrap_or(1);
            }
            Rule::die_size => {
                die_size = pair.as_str().parse().map_err(|_| "Invalid die size")?;
            }
            Rule::explode_indefinitely => {
                exploding = Exploding::Indefinite;
            }
            Rule::explode_once => {
                // Only set to `Once` if it's not already `Indefinite`
                if exploding == Exploding::None {
                    exploding = Exploding::Once;
                }
            }
            Rule::keep_drop => {
                let value = pair.as_str();
                if value.len() < 3 {
                    return Err(format!("Invalid keep/drop format: '{}'", value)); // Ensure there's a number after "kh", "kl", etc.
                }

                let amount = value[2..].parse::<usize>().map_err(|_| format!("Invalid keep/drop amount in '{}'", value))?;
                keep_drop = match &value[..2] {
                    "kh" => KeepDrop::KeepHighest(amount),
                    "kl" => KeepDrop::KeepLowest(amount),
                    "dh" => KeepDrop::DropHighest(amount),
                    "dl" => KeepDrop::DropLowest(amount),
                    _ => return Err(format!("Invalid keep/drop prefix: '{}'", value)),
                };
            }
            Rule::modifier => {
                let value = pair.as_str();
                modifier = value.parse::<isize>().map_err(|_| format!("Invalid modifier: {}", value))?;
            }
            _ => {}
        }
    }

    if die_size == 0 {
        return Err("Invalid dice expression (missing die size)".to_string());
    }

    Ok(DiceRoll::new(num_dice, die_size, exploding, keep_drop, modifier))
}

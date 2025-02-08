use pest::Parser;
use pest_derive::Parser;

mod dice;
pub use dice::{DiceRoll, Exploding, KeepDrop};

#[derive(Parser)]
#[grammar = "dice.pest"]
pub struct DiceParser;

pub fn parse_dice(input: &str) -> Result<Vec<DiceRoll>, String> {
    let mut dice_rolls = Vec::new();
    let mut last_operator = 1; // Default to positive modifier
    let mut pending_modifier: Option<isize> = None; // Holds modifier until it's assigned

    let pairs = DiceParser::parse(Rule::full_expr, input)
        .map_err(|e| format!("Parsing error: {}", e))?
        .next()
        .ok_or_else(|| "Empty input".to_string())?
        .into_inner();

    let mut current_dice_roll: Option<DiceRoll> = None;

    for pair in pairs {
        match pair.as_rule() {
            Rule::dice_expr => {
                let mut num_dice = 0;
                let mut die_size = 0;
                let mut exploding = Exploding::None;
                let mut keep_drop = KeepDrop::None;
                let mut modifier = 0;

                for inner_pair in pair.into_inner() {
                    match inner_pair.as_rule() {
                        Rule::num_dice => {
                            num_dice = inner_pair.as_str().parse().map_err(|_| "Invalid number of dice")?;
                        }
                        Rule::die_size => {
                            die_size = inner_pair.as_str().parse().map_err(|_| "Invalid die size")?;
                        }
                        Rule::explode_indefinitely => {
                            exploding = Exploding::Indefinite;
                        }
                        Rule::explode_once => {
                            if exploding == Exploding::None {
                                exploding = Exploding::Once;
                            }
                        }
                        Rule::keep_drop => {
                            let value = inner_pair.as_str();
                            let amount = value[2..].parse::<usize>()
                                .map_err(|_| format!("Invalid keep/drop amount in '{}'", value))?;
                            keep_drop = match &value[..2] {
                                "kh" => KeepDrop::KeepHighest(amount),
                                "kl" => KeepDrop::KeepLowest(amount),
                                "dh" => KeepDrop::DropHighest(amount),
                                "dl" => KeepDrop::DropLowest(amount),
                                _ => return Err(format!("Invalid keep/drop prefix: '{}'", value)),
                            };
                        }
                        Rule::modifier => {
                            let value = inner_pair.as_str();
                            modifier = value.parse::<isize>().map_err(|_| format!("Invalid modifier: '{}'", value))?;
                        }
                        _ => {
                            println!("Unexpected rule: {:?}", inner_pair);
                        }
                    }
                }

                if die_size == 0 {
                    return Err("Invalid dice expression (missing die size)".to_string());
                }

                // Apply any pending modifier before pushing a new dice roll
                if let Some(pending) = pending_modifier.take() {
                    modifier += pending;
                }

                current_dice_roll = Some(DiceRoll::new(
                    num_dice,
                    die_size,
                    exploding,
                    keep_drop,
                    modifier * last_operator,
                ));

                last_operator = 1; // Reset operator after parsing a dice expression
            }
            Rule::operator => {
                // If an operator is found, we must push the current dice roll before processing the next one
                if let Some(dice_roll) = current_dice_roll.take() {
                    dice_rolls.push(dice_roll);
                }
                last_operator = if pair.as_str() == "-" { -1 } else { 1 };
            }
            Rule::modifier => {
                let modifier_value = pair.as_str().parse::<isize>()
                    .map_err(|_| format!("Invalid standalone modifier: '{}'", pair.as_str()))?;

                pending_modifier = Some(modifier_value * last_operator);
                last_operator = 1; // Reset operator after modifier
            }
            _ => {
                println!("Unexpected rule: {:?}", pair);
            }
        }
    }

    // Ensure the last parsed dice roll is added to the list
    if let Some(dice_roll) = current_dice_roll {
        dice_rolls.push(dice_roll);
    }

    // Ensure last modifier is applied to the last dice expression
    if let Some(pending) = pending_modifier {
        if let Some(last) = dice_rolls.last_mut() {
            last.modifier += pending;
        } else {
            return Err("Modifier found with no preceding dice expression".to_string());
        }
    }

    Ok(dice_rolls)
}

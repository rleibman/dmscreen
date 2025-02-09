use dice_parser::{parse, Expr};
use std::io::{self, Write};

fn main() {
    loop {
        print!("Enter a dice roll expression (or 'exit' to quit): ");
        io::stdout().flush().unwrap();

        let mut input = String::new();
        io::stdin().read_line(&mut input).unwrap();
        let input = input.trim();

        if input.eq_ignore_ascii_case("exit") {
            println!("Goodbye!");
            break;
        }

        match parse(input) {
            Ok((expr, rolls)) => {
                println!("\nParsed Expression Tree:");
                print_expr(&expr, 0);

                println!("\nParsed Dice Rolls:");
                for (i, roll) in rolls.iter().enumerate() {
                    println!(
                        "  Roll {}: {}d{} {:?} {:?} Modifier: {}",
                        i + 1,
                        roll.num_dice,
                        roll.die_size,
                        roll.exploding,
                        roll.keep_drop,
                        roll.modifier
                    );
                }
                println!();
            }
            Err(err) => {
                println!("Error: {}", err);
            }
        }
    }
}

/// Recursively prints the expression tree for better visualization
fn print_expr(expr: &Expr, indent: usize) {
    let prefix = " ".repeat(indent * 2);
    match expr {
        Expr::Number(n) => println!("{}Number: {}", prefix, n),
        Expr::DiceRoll(roll) => println!(
            "{}Dice Roll: {}d{} {:?} {:?} Modifier: {}",
            prefix, roll.num_dice, roll.die_size, roll.exploding, roll.keep_drop, roll.modifier
        ),
        Expr::BinaryOp(left, op, right) => {
            println!("{}BinaryOp: {}", prefix, op);
            print_expr(left, indent + 1);
            print_expr(right, indent + 1);
        }
        Expr::Function(name, arg) => {
            println!("{}Function: {}", prefix, name);
            print_expr(arg, indent + 1);
        }
    }
}

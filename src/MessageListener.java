import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

//Для удобства и скорости работы, будем сразу хранить наши команды в HashMap:
public class MessageListener {

  //Map который хранит как ключ команду
  //А как значение функцию которая будет обрабатывать команду
  private static final Map<String, Method> commands = new HashMap<>();

  //Объект класса с командами (по сути нужен нам для рефлекции), чтобы вызывать метод нашего класса
  private static final CommandListener listener = new CommandListener();

  static {
    //Заполняем мапу с командами из нашего класса с командами
    //Берем список всех методов в классе CommandListener
    for (Method m : listener.getClass().getDeclaredMethods()) {
      //Смотрим, есть ли у метода нужная нам Аннотация @Command
      if (m.isAnnotationPresent(Command.class)) {
        //Берем объект нашей Аннотации
        Command cmd = m.getAnnotation(Command.class);
        //Кладем в качестве ключа нашей карты параметр name()Определенный у нашей аннотации,
        //m — переменная, хранящая наш метод – путь к нему
        commands.put(cmd.name(), m);

        //Также заносим каждый элемент aliases
        //Как ключ указывающий на тот же самый метод.
        for (String s : cmd.aliases()) {
          commands.put(s, m);
        }
      }
    }
  }

  public void onMessageReceived(MessageReceivedEvent event) {

    //Убираем чувствительность к регистру (Бот, бот и т.д.)
    String message = event.getMessage().toString().toLowerCase();
    if (message.startsWith("бот")) {
      try {
        //получим массив {"Бот", "(команду)", "аргумент1", "аргумент2",... "аргументN"
        String[] args = message.split(" ");
        //Для удобства уберем "бот" и отделим команду от аргументов

        String command = args[1].toLowerCase();
        //только аргументы команды – убираем слово бот и саму команду
        String[] nArgs = Arrays.copyOfRange(args, 2, args.length);
        //Получили command = "(команда)"; nArgs = {"аргумент1", "аргумент2",..."аргументN"};
        //Получаем метод(класс рефлексии) из нашей мапы по ключу - полученной команде(command), который мы в дальнейшем запустим для обработки полученной команды
        Method m = commands.get(command);
        //если метода нет
        if (m == null) {
          //(вывод помощи)
          return;
        }
        Command com = m.getAnnotation(Command.class);
        if (nArgs.length < com.minArgs()) {
          //что-то если аргументов меньше чем нужно
        } else if (nArgs.length > com.maxArgs()) {
          //что-то если аргументов больше чем нужно
        }
        //Через рефлексию вызываем нашу функцию-обработчик
        //invoke(Object, Object[]) где Object- Объект, для которого нужно вызвать метод
        // Object[] Список аргументов(параметров) для вызываемого метода или конструктора
        //Именно потому что мы всегда передаем nArgs у функции должен быть параметр String[] args — иначе она просто не будет найдена;
        m.invoke(listener, nArgs);
      } catch (ArrayIndexOutOfBoundsException e) {
        //Вывод списка команд или какого-либо сообщения
        //В случае если просто написать "Бот"
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }
}

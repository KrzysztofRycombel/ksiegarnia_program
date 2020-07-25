package ksiegarnia;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.sql.*;

class Okno extends JFrame {
    // dane do nawiązania komunikacji z bazą danych
    public String jdbcUrl = "jdbc:mysql://localhost:3306/ksiegarnia", jdbcUser = "root", jdbcPass = "";
    // pole na komunikaty od aplikacji
    private JTextField komunikat = new JTextField();
    // panel z zakładkami
    private JTabbedPane tp = new JTabbedPane();
    private JPanel p_kli = new JPanel(); // klienci
    private JPanel p_ksi = new JPanel(); // ksiązki
    private JPanel p_zam = new JPanel(); // zamówiemia
    // panel dla zarządzania klientami
    private JTextField pole_pesel = new JTextField();
    private JTextField pole_im = new JTextField();
    private JTextField pole_naz = new JTextField();
    private JTextField pole_ur = new JTextField();
    private JTextField pole_mail = new JTextField();
    private JTextField pole_adr = new JTextField();
    private JTextField pole_tel = new JTextField();
    private JButton przyc_zapisz_kli = new JButton("zapisz");
    private JButton przyc_usun_kli = new JButton("usuń");
    private DefaultListModel<String> lmodel_kli = new DefaultListModel<>();
    private JList<String> l_kli = new JList<>(lmodel_kli);
    private JScrollPane sp_kli = new JScrollPane(l_kli);
    // panel dla zarzadzania ksiazkami
    private JTextField pole_isbn = new JTextField();
    private JTextField pole_autor = new JTextField();
    private JTextField pole_tytul = new JTextField();
    private String[] typy=  { "sensacja", "kryminał", "fantastyka", "thriller", "horror", "obyczajowa", "poradnik", "biografia", "historyczna", "podróże", "romans", "popularnonaukowa", "młodzieżowa", "dziecięca", "reportaż", "podręcznik" };
    private JComboBox pole_typ = new JComboBox(typy);
    private JTextField pole_wydawnictwo = new JTextField();
    private JTextField pole_rok = new JTextField();
    private JTextField pole_cena = new JTextField();
    private JTextField pole_nowa_cena = new JTextField();
    private JButton przyc_zapisz_ksi = new JButton("zapisz");
    private JButton przyc_edycja_ksi = new JButton("edytuj cene");
    private JButton przyc_usun_ksi = new JButton("usuń");
    private DefaultListModel<String> lmodel_ksi= new DefaultListModel<>();
    private JList<String> l_ksi = new JList<>(lmodel_ksi);
    private JScrollPane sp_ksi = new JScrollPane(l_ksi);
    // panel dla zarzadzania zamoweniami
    private JTextField pole_zam_pesel = new JTextField();
    private JTextField pole_zam_isbn = new JTextField();
    private JTextField pole_zam_ilosc = new JTextField();
    private String[] status_zamowienia=  { "oczekuje", "wysłane", "zapłacone" };
    private JComboBox pole_zam_status = new JComboBox(status_zamowienia);
    private JTextField pole_zam_cena = new JTextField();
    private JButton przyc_zapisz_zam = new JButton("zapisz");
    private JButton przyc_edycja_zam = new JButton("edytuj status zamowienia");
    private DefaultListModel<String> lmodel_zam= new DefaultListModel<>();
    private JList<String> l_zam = new JList<>(lmodel_zam);
    private JScrollPane sp_zam = new JScrollPane(l_zam);

    //funkcja aktualizujaca liste zamowien
    private void AktualnaListaZamowien(JList<String> lis) {
        try (Connection conn=DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            Statement stmt = conn.createStatement();
            String sql = "Select * from zamowienia ORDER BY id";
            ResultSet res = stmt.executeQuery(sql);
            lmodel_ksi.clear();
            while(res.next()) {
                String s = res.getString(1) + ": " + res.getString(2) + " " + res.getString(3) + ", " + res.getString(4);
                lmodel_zam.addElement(s);
            }
        }
        catch (SQLException ex) {
            komunikat.setText("nie udało się zaktualizować listy zamowien");
        }
    }

    // delegat obsługujący zdarzenie akcji od przycisku 'dodaj zamowienie'
    private ActionListener akc_zap_zam = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String pesel = pole_zam_pesel.getText();
            String isbn = pole_zam_isbn.getText();
            if (! isbn.matches("[0-9]{13}") || ! pesel.matches("[0-9]{3,11}")) {
                JOptionPane.showMessageDialog(Okno.this, "błąd w polu z isbn lub pesel");
                pole_zam_isbn.setText("");
                pole_zam_pesel.setText("");
                return;
            }
            System.out.println("testuje1");
            try (Connection conn=DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();
                System.out.println("testuje2");
                String sql1 = "SELECT count(*) FROM klienci WHERE pesel = "+pole_zam_pesel.getText();
                ResultSet res = stmt.executeQuery(sql1);
                res.next();
                int k = res.getInt(1);

                if (k !=0) {
                    String sql2 = "SELECT count(*) FROM ksiazki WHERE isbn = "+pole_zam_isbn.getText();
                    ResultSet res1 = stmt.executeQuery(sql2);
                    res1.next();
                    int l = res1.getInt(1);
                    if(l != 0)
                    {

                    }
                    else
                    {
                        komunikat.setText("błąd SQL - nie znaleziono takiej ksiazki");
                        return;
                    }
                }
                else
                {
                    komunikat.setText("błąd SQL - nie znaleziono takiego klienta");
                    return;
                }
            }
            catch(SQLException ex) {
                System.out.println(ex.getMessage());
                komunikat.setText("błąd SQL - nie dodano zamowienia1");
                return;
            }
            String ilosc = pole_zam_ilosc.getText();
            String cena = pole_zam_cena.getText();
            if (ilosc.equals("") || cena.equals("")) {
                JOptionPane.showMessageDialog(Okno.this, "nie wypełnione pole z iloscia lub cena");
                return;
            }
            try (Connection conn=DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();

                String sql1 = "INSERT INTO zamowienia (pesel, kiedy, status) VALUES(" + pole_zam_pesel.getText() + ", NOW()"+", 'oczekuje')";
                int res = stmt.executeUpdate(sql1);
                System.out.println(sql1);
                String sql2 = "INSERT INTO zestawienia (isbn, cena) VALUES("+pole_zam_isbn.getText()+", "+pole_zam_cena.getText()+")";
                int res1 = stmt.executeUpdate(sql2);
                System.out.println(sql2);
                lmodel_zam.clear();
                if (res == 1 && res1 == 1) {

                    komunikat.setText("OK - zamowienie dodane do bazy");
                    AktualnaListaZamowien(l_zam);
                }
                else
                {
                    komunikat.setText("błąd SQL - nie dodano zamowienia2");
                }
            }
            catch(SQLException ex) {
                System.out.println(ex.getMessage());
                komunikat.setText("błąd SQL - nie dodano zamowienia3");
            }
        }
    };
    // delegat obsługujący zdarzenie akcji od przycisku 'edytuj zamowienie'
    private ActionListener akc_edytuj_zam = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String status_zam = pole_zam_status.getSelectedItem().toString();
            System.out.println(status_zam);
            String p = l_zam.getModel().getElementAt(l_zam.getSelectionModel().getMinSelectionIndex());
            p = p.substring(0, p.indexOf(':'));

            try (Connection conn=DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();
                String sql = "SELECT COUNT(*) FROM zamowienia WHERE id = " + p;
                System.out.println(sql);
                stmt.executeQuery(sql);
                    String sql1 = "UPDATE zamowienia SET status = '" + pole_zam_status.getSelectedItem().toString() + "' where id = "+p;
                    System.out.println(sql1);
                    stmt.executeUpdate(sql1);
                    komunikat.setText("OK - status zamowienia zaktualizowany");
                    lmodel_zam.clear();
                    AktualnaListaZamowien(l_zam);

            }
            catch (SQLException ex) {
                System.out.println(ex.getMessage());
                komunikat.setText("błąd SQL - nie zaktualizowalem zamowienia");
            }
        }
    };
    //funkcja aktualizujaca liste ksiazek
    private void AktualnaListaKsiazek(JList<String> lis) {
        try (Connection conn=DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            Statement stmt = conn.createStatement();
            String sql = "Select * from ksiazki ORDER BY autor, tytul";
            ResultSet res = stmt.executeQuery(sql);
            lmodel_ksi.clear();
            while(res.next()) {
                System.out.println("wyswietlanie");
                String s = res.getString(1) + ": " + res.getString(2) + " " + res.getString(3) + ", " + res.getString(4)+ ", wyd. " + res.getString(5)+ ", rok  " + res.getString(6)+ ", " + res.getString(7)+" zł";
                lmodel_ksi.addElement(s);
            }
        }
        catch (SQLException ex) {
            komunikat.setText("nie udało się zaktualizować listy klientów");
        }
    }
    // delegat obsługujący zdarzenie akcji od przycisku 'zapisz ksiazke'
    private ActionListener akc_zap_ksi = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String isbn = pole_isbn.getText();
            if (! isbn.matches("[0-9]{13}")) {
                JOptionPane.showMessageDialog(Okno.this, "błąd w polu z isbn");
                pole_isbn.setText("");
                pole_isbn.requestFocus();
                return;
            }
            String autor = pole_autor.getText();
            String tytul = pole_tytul.getText();
            String typ = pole_typ.getSelectedItem().toString();
            if (autor.equals("") || tytul.equals("") || pole_typ.getSelectedItem().toString().equals("")) {
                JOptionPane.showMessageDialog(Okno.this, "nie wypełnione pole z autorem lub tytulem lub typem ksiazki");
                return;
            }
            String wydawnictwo = pole_wydawnictwo.getText();
            String rok = pole_rok.getText();
            String cena = pole_cena.getText();
            if (wydawnictwo.equals("") || rok.equals("") || cena.equals("")) {
                JOptionPane.showMessageDialog(Okno.this, "nie wypełnione pole z wydawnictwem lub rokiem lub cena");
                return;
            }
            try (Connection conn=DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();

                String sql1 = "INSERT INTO ksiazki (isbn, autor, tytul, typ , wydawnictwo, rok, cena) VALUES('" + pole_isbn.getText() + "', '" + pole_autor.getText() + "', '" + pole_tytul.getText() + "', '" + pole_typ.getSelectedItem().toString()+ "', '" + pole_wydawnictwo.getText() + "', '" + pole_rok.getText() + "', '" + pole_cena.getText() + "')";
                int res = stmt.executeUpdate(sql1);
                System.out.printf("Dodalem do tabeli ksiazki "+ pole_isbn.getText()+ "   "+pole_autor.getText()+"   "+pole_tytul.getText()+"   "+pole_typ.getSelectedItem().toString() + pole_wydawnictwo.getText() + pole_rok.getText() + pole_cena.getText() + "\r\n");
                System.out.println(res);
                if (res == 1) {

                    komunikat.setText("OK - klient dodany do bazy");
                    System.out.printf("Dodalem do tabeli kontakty "+ pole_pesel.getText()+ "   "+pole_mail.getText()+"   "+pole_adr.getText()+"   "+pole_tel.getText());
                    System.out.println("Aktualizuje");
                    AktualnaListaKsiazek(l_ksi);
                }
            }
            catch(SQLException ex) {
                System.out.println(ex.getMessage());
                komunikat.setText("błąd SQL - nie zapisano ksiazki");
            }
        }
    };
    // delegat obsługujący zdarzenie akcji od przycisku 'edytuj cena'
    private ActionListener akc_edytuj_ksi = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String nowa_cena = pole_nowa_cena.getText();
            System.out.println(nowa_cena);
            if ( !pole_nowa_cena.getText().matches("-?\\d+(\\.\\d+)?") || pole_nowa_cena.getText().equals("")) {
                JOptionPane.showMessageDialog(Okno.this, "błąd w polu nowa cena lub pole jest puste");
                pole_nowa_cena.setText("");
                pole_nowa_cena.requestFocus();
                return;
            }
            String p = l_ksi.getModel().getElementAt(l_ksi.getSelectionModel().getMinSelectionIndex());
            p = p.substring(0, p.indexOf(':'));
            System.out.println(p);
            try (Connection conn=DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();
                String sql = "SELECT COUNT(*) FROM zestawienia WHERE isbn = '" + p + "'";
                ResultSet res = stmt.executeQuery(sql);
                res.next();
                int k = res.getInt(1);

                if (k == 0) {
                    System.out.println(p);
                    String sql1 = "UPDATE ksiazki SET cena = " + nowa_cena + " where isbn = "+p;
                    System.out.println(sql1);
                    stmt.executeUpdate(sql1);
                    System.out.printf("\r\nZedytowalem z tabeli ksiazki cene z nr isbn = "+p);
                    komunikat.setText("OK - cena ksiazki zaktualizowana");
                    System.out.println("Aktualizuje");
                    AktualnaListaKsiazek(l_ksi);
                }
                else komunikat.setText("nie zaktualizowano ksiazki, ponieważ zostala już zamówienia");
            }
            catch (SQLException ex) {
                System.out.println(ex.getMessage());
                komunikat.setText("błąd SQL - nie zdedytowana ksiazki");
            }
        }
    };
    // delegat obsługujący zdarzenie akcji od przycisku 'usuń ksiazke'
    private ActionListener akc_usun_ksi = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {

            String p = l_ksi.getModel().getElementAt(l_ksi.getSelectionModel().getMinSelectionIndex());
            p = p.substring(0, p.indexOf(':'));
            System.out.println(p);
            try (Connection conn=DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();
                String sql = "SELECT COUNT(*) FROM zestawienia WHERE isbn = '" + p + "'";
                ResultSet res = stmt.executeQuery(sql);
                res.next();
                int k = res.getInt(1);

                if (k == 0) {
                    System.out.println(p);
                    String sql1 = "DELETE FROM ksiazki WHERE isbn = '" + p + "'";
                    stmt.executeUpdate(sql1);
                    System.out.printf("\r\nUsunalem z tabeli ksiazki wpis z nr isbn = "+p);
                    komunikat.setText("OK - ksiazka usunięty bazy");
                    System.out.println("Aktualizuje");
                    AktualnaListaKsiazek(l_ksi);
                }
                else komunikat.setText("nie usunięto ksiazki, ponieważ zostala już zamówienia");
            }
            catch (SQLException ex) {
                komunikat.setText("błąd SQL - nie ununięto ksiazki");
            }
        }
    };


    // funkcja aktualizująca listę klientów
    private void AktualnaListaKlientów(JList<String> lis) {
        try (Connection conn=DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT klienci.pesel, nazwisko, imie, adres FROM klienci, kontakty WHERE klienci.pesel = kontakty.pesel ORDER BY nazwisko, imie";
            ResultSet res = stmt.executeQuery(sql);
            lmodel_kli.clear();
            while(res.next()) {
                String s = res.getString(1) + ": " + res.getString(2) + " " + res.getString(3) + ", " + res.getString(4);
                lmodel_kli.addElement(s);
            }
        }
        catch (SQLException ex) {
            komunikat.setText("nie udało się zaktualizować listy klientów");
        }
    }
    // delegat obsługujący zdarzenie akcji od przycisku 'zapisz klienta'
    private ActionListener akc_zap_kli = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String pesel = pole_pesel.getText();
            if (! pesel.matches("[0-9]{3,11}")) {
                JOptionPane.showMessageDialog(Okno.this, "błąd w polu z peselm");
                pole_pesel.setText("");
                pole_pesel.requestFocus();
                return;
            }
            String imie = pole_im.getText();
            String nazwisko = pole_naz.getText();
            String ur = pole_ur.getText();
            if (imie.equals("") || nazwisko.equals("") || ur.equals("")) {
                JOptionPane.showMessageDialog(Okno.this, "nie wypełnione pole z imieniem lub nazwiskiem lub datą urodzenia");
                return;
            }
            String mail = pole_mail.getText();
            String adr = pole_adr.getText();
            String tel = pole_tel.getText();
            if (mail.equals("") || adr.equals("")) {
                JOptionPane.showMessageDialog(Okno.this, "nie wypełnione pole z emailem lub adresem");
                return;
            }
            try (Connection conn=DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();

                String sql1 = "INSERT INTO klienci (pesel, imie, nazwisko, ur) VALUES('" + pole_pesel.getText() + "', '" + pole_im.getText() + "', '" + pole_naz.getText() + "', '" + pole_ur.getText() + "')";
                int res = stmt.executeUpdate(sql1);
                System.out.printf("Dodalem do tabeli klienci "+ pole_pesel.getText()+ "   "+pole_im.getText()+"   "+pole_naz.getText()+"   "+pole_ur.getText()+"\r\n");
                if (res == 1) {
                    komunikat.setText("OK - klient dodany do bazy");
                    String sql2 = "INSERT INTO kontakty (pesel, mail, adres, tel) VALUES('" + pole_pesel.getText() + "', '" + pole_mail.getText() + "', '" + pole_adr.getText() + "', '" + pole_tel.getText() + "')";
                    stmt.executeUpdate(sql2);
                    System.out.printf("Dodalem do tabeli kontakty "+ pole_pesel.getText()+ "   "+pole_mail.getText()+"   "+pole_adr.getText()+"   "+pole_tel.getText());
                    AktualnaListaKlientów(l_kli);
                }
            }
            catch(SQLException ex) {
                System.out.println(ex.getMessage());
                komunikat.setText("błąd SQL - nie zapisano klienta");
            }
        }
    };
    // delegat obsługujący zdarzenie akcji od przycisku 'usuń klienta'
    private ActionListener akc_usun_kli = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {

            String p = l_kli.getModel().getElementAt(l_kli.getSelectionModel().getMinSelectionIndex());
            p = p.substring(0, p.indexOf(':'));
            try (Connection conn=DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();
                String sql = "SELECT COUNT(*) FROM zamowienia WHERE pesel = '" + p + "'";
                ResultSet res = stmt.executeQuery(sql);
                res.next();
                int k = res.getInt(1);
                if (k == 0) {
                    String sql1 = "DELETE FROM klienci WHERE pesel = '" + p + "'";
                    stmt.executeUpdate(sql1);
                    String sql2 = "DELETE FROM kontakty WHERE pesel = '" + p + "'";
                    System.out.printf("\r\nUsunalem z tabeli klienci i kontakty wpis z nr pesel = "+p);
                    stmt.executeUpdate(sql2);
                    komunikat.setText("OK - klient usunięty bazy");
                    AktualnaListaKlientów(l_kli);
                }
                else komunikat.setText("nie usunięto klienta, ponieważ składał już zamówienia");
            }
            catch (SQLException ex) {
                komunikat.setText("błąd SQL - nie ununięto klienta");
            }
        }
    };

    public Okno() throws SQLException {
        super("Księgarnia wysyłkowa");
        setSize(660, 460);
        setLocation(100, 100);
        setResizable(false);
        //panel do zarzadzania zamowieniami
        p_zam.setLayout(null);
        //pole z peselem
        JLabel lab16 = new JLabel("pesel:");
        p_zam.add(lab16);
        lab16.setSize(100, 20);
        lab16.setLocation(40, 40);
        lab16.setHorizontalTextPosition(JLabel.RIGHT);
        p_zam.add(pole_zam_pesel);
        pole_zam_pesel.setSize(200, 20);
        pole_zam_pesel.setLocation(160, 40);
        // pole z isbn
        JLabel lab17 = new JLabel("isbn:");
        p_zam.add(lab17);
        lab17.setSize(100, 20);
        lab17.setLocation(40, 80);
        lab17.setHorizontalTextPosition(JLabel.RIGHT);
        p_zam.add(pole_zam_isbn);
        pole_zam_isbn.setSize(200, 20);
        pole_zam_isbn.setLocation(160, 80);
        // pole z iloscia ksiazek
        JLabel lab18 = new JLabel("ilosc ksiazek:");
        p_zam.add(lab18);
        lab18.setSize(100, 20);
        lab18.setLocation(40, 120);
        lab18.setHorizontalTextPosition(JLabel.RIGHT);
        p_zam.add(pole_zam_ilosc);
        pole_zam_ilosc.setSize(200, 20);
        pole_zam_ilosc.setLocation(160, 120);
        // pole z cena
        JLabel lab19 = new JLabel("cena zamowienia:");
        p_zam.add(lab19);
        lab19.setSize(100, 20);
        lab19.setLocation(40, 160);
        lab19.setHorizontalTextPosition(JLabel.RIGHT);
        p_zam.add(pole_zam_cena);
        pole_zam_cena.setSize(200, 20);
        pole_zam_cena.setLocation(160, 160);
        //pole ze statusem zamowienia
        JLabel lab20 = new JLabel("status zamowienia:");
        p_zam.add(lab20);
        lab20.setSize(150, 20);
        lab20.setLocation(40, 270);
        lab20.setHorizontalTextPosition(JLabel.RIGHT);
        p_zam.add(pole_zam_status);
        pole_zam_status.setSize(100, 20);
        pole_zam_status.setLocation(160, 270);
        // przycisk do dodania zamowienia
        p_zam.add(przyc_zapisz_zam);
        przyc_zapisz_zam.setSize(200, 20);
        przyc_zapisz_zam.setLocation(160, 200);
        przyc_zapisz_zam.addActionListener(akc_zap_zam);
        // przycisk do edycji statusu zamowienia
        p_zam.add(przyc_edycja_zam);
        przyc_edycja_zam.setSize(200, 20);
        przyc_edycja_zam.setLocation(50, 300);
        przyc_edycja_zam.addActionListener(akc_edytuj_zam);
        // lista z zamowieniami
        p_zam.add(sp_zam);
        sp_zam.setSize(200, 260);
        sp_zam.setLocation(400, 40);
        l_zam.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        AktualnaListaZamowien(l_zam);
        //*****************************************************************************************************
        // panel do zarządzania ksiazkami
        p_ksi.setLayout(null);
        // pole z isbn
        JLabel lab8 = new JLabel("isbn:");
        p_ksi.add(lab8);
        lab8.setSize(100, 20);
        lab8.setLocation(40, 40);
        lab8.setHorizontalTextPosition(JLabel.RIGHT);
        p_ksi.add(pole_isbn);
        pole_isbn.setSize(200, 20);
        pole_isbn.setLocation(160, 40);
        // pole z autorem
        JLabel lab9 = new JLabel("autor:");
        p_ksi.add(lab9);
        lab9.setSize(100, 20);
        lab9.setLocation(40, 80);
        lab9.setHorizontalTextPosition(JLabel.RIGHT);
        p_ksi.add(pole_autor);
        pole_autor.setSize(200, 20);
        pole_autor.setLocation(160, 80);
        // pole z tytulem
        JLabel lab10 = new JLabel("tytul:");
        p_ksi.add(lab10);
        lab10.setSize(100, 20);
        lab10.setLocation(40, 120);
        lab10.setHorizontalTextPosition(JLabel.RIGHT);
        p_ksi.add(pole_tytul);
        pole_tytul.setSize(200, 20);
        pole_tytul.setLocation(160, 120);
        // pole z typem
        JLabel lab11 = new JLabel("typ ksiazki:");
        p_ksi.add(lab11);
        lab11.setSize(100, 20);
        lab11.setLocation(40, 160);
        lab11.setHorizontalTextPosition(JLabel.RIGHT);
        p_ksi.add(pole_typ);
        pole_typ.setSize(200, 20);
        pole_typ.setLocation(160, 160);
        // pole z wydawnictwem
        JLabel lab12 = new JLabel("wydawnictwo:");
        p_ksi.add(lab12);
        lab12.setSize(100, 20);
        lab12.setLocation(40, 200);
        lab12.setHorizontalTextPosition(JLabel.RIGHT);
        p_ksi.add(pole_wydawnictwo);
        pole_wydawnictwo.setSize(200, 20);
        pole_wydawnictwo.setLocation(160, 200);
        // pole z rokiem
        JLabel lab13 = new JLabel("rok wydania:");
        p_ksi.add(lab13);
        lab13.setSize(100, 20);
        lab13.setLocation(40, 240);
        lab13.setHorizontalTextPosition(JLabel.RIGHT);
        p_ksi.add(pole_rok);
        pole_rok.setSize(200, 20);
        pole_rok.setLocation(160, 240);
        // pole z cena
        JLabel lab14 = new JLabel("cena:");
        p_ksi.add(lab14);
        lab14.setSize(100, 20);
        lab14.setLocation(40, 280);
        lab14.setHorizontalTextPosition(JLabel.RIGHT);
        p_ksi.add(pole_cena);
        pole_cena.setSize(200, 20);
        pole_cena.setLocation(160, 280);
        // pole z nowa cena
        JLabel lab15 = new JLabel("Nowa cena:");
        p_ksi.add(lab15);
        lab15.setSize(100, 20);
        lab15.setLocation(160, 350);
        lab15.setHorizontalTextPosition(JLabel.RIGHT);
        p_ksi.add(pole_nowa_cena);
        pole_nowa_cena.setSize(100, 20);
        pole_nowa_cena.setLocation(260, 350);
        // przycisk do zapisu ksiazki
        p_ksi.add(przyc_zapisz_ksi);
        przyc_zapisz_ksi.setSize(150, 20);
        przyc_zapisz_ksi.setLocation(50, 320);
        przyc_zapisz_ksi.addActionListener(akc_zap_ksi);
        // przycisk do edycji ksiazki
        p_ksi.add(przyc_edycja_ksi);
        przyc_edycja_ksi.setSize(150, 20);
        przyc_edycja_ksi.setLocation(220, 320);
        przyc_edycja_ksi.addActionListener(akc_edytuj_ksi);
        // przycisk do usunięcia ksiazki
        p_ksi.add(przyc_usun_ksi);
        przyc_usun_ksi.setSize(150, 20);
        przyc_usun_ksi.setLocation(390, 320);
        przyc_usun_ksi.addActionListener(akc_usun_ksi);
        // lista z klientami
        p_ksi.add(sp_ksi);
        sp_ksi.setSize(200, 260);
        sp_ksi.setLocation(400, 40);
        l_ksi.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        AktualnaListaKsiazek(l_ksi);
        // panel do zarządzania klientami
        p_kli.setLayout(null);
        // pole z peselem
        JLabel lab1 = new JLabel("pesel:");
        p_kli.add(lab1);
        lab1.setSize(100, 20);
        lab1.setLocation(40, 40);
        lab1.setHorizontalTextPosition(JLabel.RIGHT);
        p_kli.add(pole_pesel);
        pole_pesel.setSize(200, 20);
        pole_pesel.setLocation(160, 40);
        // pole z imieniem
        JLabel lab2 = new JLabel("imię:");
        p_kli.add(lab2);
        lab2.setSize(100, 20);
        lab2.setLocation(40, 80);
        lab2.setHorizontalTextPosition(JLabel.RIGHT);
        p_kli.add(pole_im);
        pole_im.setSize(200, 20);
        pole_im.setLocation(160, 80);
        // pole z nazwiskiem
        JLabel lab3 = new JLabel("nazwisko:");
        p_kli.add(lab3);
        lab3.setSize(100, 20);
        lab3.setLocation(40, 120);
        lab3.setHorizontalTextPosition(JLabel.RIGHT);
        p_kli.add(pole_naz);
        pole_naz.setSize(200, 20);
        pole_naz.setLocation(160, 120);
        // pole z datą urodzenia
        JLabel lab4 = new JLabel("data urodzenia:");
        p_kli.add(lab4);
        lab4.setSize(100, 20);
        lab4.setLocation(40, 160);
        lab4.setHorizontalTextPosition(JLabel.RIGHT);
        p_kli.add(pole_ur);
        pole_ur.setSize(200, 20);
        pole_ur.setLocation(160, 160);
        // pole z mailem
        JLabel lab5 = new JLabel("mail:");
        p_kli.add(lab5);
        lab5.setSize(100, 20);
        lab5.setLocation(40, 200);
        lab5.setHorizontalTextPosition(JLabel.RIGHT);
        p_kli.add(pole_mail);
        pole_mail.setSize(200, 20);
        pole_mail.setLocation(160, 200);
        // pole z adresem
        JLabel lab6 = new JLabel("adres:");
        p_kli.add(lab6);
        lab6.setSize(100, 20);
        lab6.setLocation(40, 240);
        lab6.setHorizontalTextPosition(JLabel.RIGHT);
        p_kli.add(pole_adr);
        pole_adr.setSize(200, 20);
        pole_adr.setLocation(160, 240);
        // pole z telefonem
        JLabel lab7 = new JLabel("telefon:");
        p_kli.add(lab7);
        lab7.setSize(100, 20);
        lab7.setLocation(40, 280);
        lab7.setHorizontalTextPosition(JLabel.RIGHT);
        p_kli.add(pole_tel);
        pole_tel.setSize(200, 20);
        pole_tel.setLocation(160, 280);
        // przycisk do zapisu klienta
        p_kli.add(przyc_zapisz_kli);
        przyc_zapisz_kli.setSize(200, 20);
        przyc_zapisz_kli.setLocation(160, 320);
        przyc_zapisz_kli.addActionListener(akc_zap_kli);
        // przycisk do usunięcia klienta
        p_kli.add(przyc_usun_kli);
        przyc_usun_kli.setSize(200, 20);
        przyc_usun_kli.setLocation(400, 320);
        przyc_usun_kli.addActionListener(akc_usun_kli);
        // lista z klientami
        p_kli.add(sp_kli);
        sp_kli.setSize(200, 260);
        sp_kli.setLocation(400, 40);
        l_kli.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        AktualnaListaKlientów(l_kli);
        // panel z zakładkami
        tp.addTab("klienci", p_kli);
        tp.addTab("książki", p_ksi);
        tp.addTab("zamówienia", p_zam);
        getContentPane().add(tp, BorderLayout.CENTER);
        // pole na komentarze
        komunikat.setEditable(false);
        getContentPane().add(komunikat, BorderLayout.SOUTH);
        // pokazanie okna
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }
}

public class Ksiegarnia {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        new Okno();
    }
}
